package me.developes.humming.sdui.common

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SDUIFormViewModel(
    private val handle: SavedStateHandle, libraries: List<SDLibrary> = listOf(),
    val screenStateMode: ScreenStateMode = ScreenStateMode.LEGACY_SHARED,
    screenInputs: ScreenInputs = ScreenInputs(),
    val screenEntryId: String? = null,
    val entryInputs: (entryId: String, route: String) -> ScreenInputs = { _, _ -> ScreenInputs() },
) : ViewModel() {
    private var currentInputs = screenInputs
    val screenInputs: ScreenInputs get() = currentInputs
    private val _runningTasks = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val legacyBindings = mutableMapOf<StateFlow<*>, Job>()

    init {
        viewModelScope.launch {
            _runningTasks.map { it > 0 }.distinctUntilChanged().collectLatest { running ->
                if (running) delay(100)
                _isLoading.value = running
            }
        }
    }

    /** Releases an imperative legacy binding before the ViewModel is cleared. */
    fun releaseBinding(binding: StateFlow<*>) {
        legacyBindings.remove(binding)?.cancel()
    }

    override fun onCleared() {
        legacyBindings.values.forEach { it.cancel() }
        legacyBindings.clear()
        typedState.clear()
        currentInputs = ScreenInputs()
        _isLoading.value = false
        super.onCleared()
    }

    fun binding(value: String?): NodeBinding<String> = binding(value) { it ?: "" }

    fun <T> binding(value: String?, calculation: (String?) -> T): NodeBinding<T> =
        NodeBinding({ calculation(resolveStatePlaceholders(value)) },
            if (value?.contains("#{") == true) onStateUpdated else flowOf(0))

    fun stateBinding(key: String, initialValue: String? = null): NodeBinding<String> {
        getState(key, initialValue)
        return NodeBinding({ resolveStatePlaceholders(formState[key]) }, onStateUpdated)
    }

    fun <T> stateBinding(key: String, initialValue: String?, calculation: (String) -> T): NodeBinding<T> {
        getState(key, initialValue)
        return NodeBinding({ calculation(resolveStatePlaceholders(formState[key])) }, onStateUpdated)
    }

    private val _errorState = MutableStateFlow<ErrorData?>(null)
    val errorState: StateFlow<ErrorData?> = _errorState.asStateFlow()

    private var mutationRevision = 0
    private var mutationListeners = emptySet<StateEventToken>()
    val onStateUpdated = MutableStateFlow(0)
    private val typedState = mutableStateMapOf<String, SduiValue>()
    private val _formState = LegacyStateAccess(typedState)
    val formState: Map<String, String> = _formState.view
    val stateValues: Map<String, SduiValue> = typedState

    fun stateValue(key: String): SduiValue = typedState[key] ?: SduiValue.Missing

    fun updateValue(key: String, value: SduiValue) = recordValue(key,value,emptySet())

    suspend fun updateValueFromAction(key: String, value: SduiValue) {
        recordValue(key,value,currentCoroutineContext()[StateMutationOrigin]?.listeners.orEmpty())
    }

    suspend fun actionValues(): ActionValues = checkNotNull(currentCoroutineContext()[ActionValuesContext]?.takeIf {
        it.owner === this
    }) { "Action values require executeActions on their owning ViewModel" }.values

    /** New entry, same registered capabilities; form values are never copied from another entry. */
    fun createScreenEntry(entryId: String, inputs: ScreenInputs = ScreenInputs()): SDUIFormViewModel =
        SDUIFormViewModel(SavedStateHandle(), libraries.values.toList(), screenStateMode, inputs, entryId, entryInputs).also { child ->
            nodeTypeProviders.forEach { (type,provider) -> child.addNodeProvider(type,provider) }
        }

    private val nodeTypeProviders: HashMap<String, NodeProvider> = HashMap()
    private val libraries: HashMap<String, SDLibrary> = HashMap<String, SDLibrary>().apply {
        libraries.forEach { library ->
            this[library.namespace] = library
        }
    }

    /** Counts nested/concurrent operations and finalizes on failure or cancellation. */
    private suspend fun <T> withRunningTask(block: suspend () -> T): T {
        _runningTasks.update { it + 1 }
        return try { block() } finally { _runningTasks.update { it - 1 } }
    }

    fun runTask(block: suspend () -> Unit, onError: (Throwable) -> ErrorData? = { null }): Job =
        viewModelScope.launch {
            withRunningTask {
                try {
                    block()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val mapped = try { onError(e) } catch (mapping: CancellationException) {
                        mapping.addSuppressed(e)
                        throw mapping
                    } catch (mapping: Exception) {
                        if (mapping !== e) e.addSuppressed(mapping)
                        null
                    }
                    val error = mapped ?: ErrorData.taskError("Task failed").copy(
                        diagnostic = NodeDiagnostic("TASK_ERROR", "Task failed", "task"),
                    )
                    setError(error.copy(cause = error.cause ?: e))
                }
            }
        }

    /**
     * Sets the current error state.
     *
     * This function updates the `_errorState` with the provided `error` object.
     * This can be used to display error messages or dialogs in the UI that are
     * bound to the `errorState`.
     *
     * @param error The [ErrorData] object representing the error to be set.
     */
    fun setError(error: ErrorData) {
        _errorState.value = error
    }

    /**
     * Dismisses the current error state.
     *
     * This function sets the `_errorState` to `null`, effectively clearing any
     * existing error. This can be used to dismiss error messages or dialogs
     * in the UI that are bound to the `errorState`.
     */
    fun dismissError() {
        _errorState.value = null
    }

    /**
     * Retrieves a value from the form state associated with the given key.
     *
     * This function takes a `key` to look up in the `_formState` map and an optional
     * `initialValue`. If the key exists in the map, its associated value is returned
     * after resolving any state placeholders. If the key does not exist, the `initialValue`
     * is used (or an empty string if `initialValue` is null), added to the map, and then
     * returned after resolving any state placeholders.
     *
     * @param key The key to look up in the form state.
     * @param initialValue An optional initial value to use if the key does not exist.
     * @return The value associated with the key, or the `initialValue` if the key does not exist,
     *         with all state placeholders resolved.
     */
    fun getState(
        key: String,
        initialValue: String? = null,
    ): String = resolveStatePlaceholders(_formState[key] ?: let {
        val value = initialValue ?: ""
        _formState[key] = value
        value
    })

    /**
     * Creates a [MutableStateFlow] that emits values from the form state.
     *
     * This function takes a `key` to look up in the `_formState` map and an optional
     * `initialValue`. It creates a [MutableStateFlow] that initially emits the value
     * associated with the key, or the `initialValue` if the key does not exist.
     *
     * The function also sets up a coroutine to listen for updates to the state. Whenever
     * the state is updated (indicated by changes in `onStateUpdated`), it re-evaluates
     * the value associated with the key, resolves any placeholders, and updates the
     * flow's value if it has changed.
     *
     * @param key The key to look up in the form state.
     * @param initialValue An optional initial value to use if the key does not exist.
     * @return A [MutableStateFlow] emitting values from the form state.
     */
    fun getStateFlow(
        key: String,
        initialValue: String? = null,
    ) = MutableStateFlow(getState(key, initialValue)).also { flow ->
        legacyBindings[flow] = viewModelScope.launch {
            onStateUpdated.collect { _ ->
                val value = resolveStatePlaceholders(_formState[key])
                if (flow.value != value) {
                    flow.value = value
                }
            }
        }
    }

    /**
     * Retrieves a value from the form state and applies a calculation function to it.
     *
     * This function takes a `key` to look up in the `_formState` map and a `calculation`
     * function that processes the retrieved value. If the key exists in the map, its
     * associated value is passed to the `calculation` function. If the key does not exist,
     * `null` is passed to the function.
     *
     * @param key The key to look up in the form state.
     * @param calculation A function that takes a nullable String and returns a value of type T.
     * @return The result of applying the `calculation` function to the value associated with the key,
     *         or to `null` if the key does not exist in the form state.
     */
    fun <T> getState(
        key: String,
        calculation: (String?) -> T,
    ): T = calculation(_formState[key])

    /**
     * Creates a [MutableStateFlow] that emits values calculated from the form state.
     *
     * This function takes a `key` to look up in the `_formState` map and a `calculation`
     * function that processes the retrieved value. It creates a [MutableStateFlow] that
     * initially emits the result of applying the `calculation` function to the value
     * associated with the key, or to `null` if the key does not exist.
     *
     * The function also sets up a coroutine to listen for updates to the state. Whenever
     * the state is updated (indicated by changes in `onStateUpdated`), it re-evaluates
     * the value associated with the key, applies the `calculation` function, and updates
     * the flow's value if it has changed.
     *
     * @param key The key to look up in the form state.
     * @param calculation A function that takes a nullable String and returns a value of type T.
     * @return A [MutableStateFlow] emitting values of type T calculated from the form state.
     */
    fun <T> getStateFlow(
        key: String,
        calculation: (String?) -> T,
    ) = MutableStateFlow(getState(key, calculation)).also { flow ->
        legacyBindings[flow] = viewModelScope.launch {
            onStateUpdated.collect { _ ->
                val value = getState(key, calculation)
                if (flow.value != value) {
                    flow.value = value
                }
            }
        }
    }

    /**
     * Updates the form state with a new key-value pair.
     *
     * If the provided value is `null`, the key is removed from the state.
     * Otherwise, the key is added or updated with the new value.
     * After a changed value, the `onStateUpdated` flow is incremented
     * to notify any observers of the change.
     *
     * @param key The key to be added or updated in the form state.
     * @param value The value to be associated with the key. If `null`, the key is removed.
     */
    fun updateState(key: String, value: String?) = recordState(key, value, emptySet())

    /** Custom suspend actions should use this API to preserve event causality. */
    suspend fun updateStateFromAction(key: String, value: String?) {
        recordState(key, value, currentCoroutineContext()[StateMutationOrigin]?.listeners.orEmpty())
    }

    private fun recordState(key: String, value: String?, listeners: Set<StateEventToken>) {
        recordValue(key, value?.let(SduiValue::Text) ?: SduiValue.Missing,listeners)
    }

    private fun recordValue(key: String, value: SduiValue, listeners: Set<StateEventToken>) {
        if (stateValue(key) == value) return
        if (value === SduiValue.Missing) typedState.remove(key) else typedState[key] = value
        mutationListeners = listeners
        mutationRevision = onStateUpdated.value + 1
        onStateUpdated.value = mutationRevision
    }

    suspend fun executeStateChangeActions(
        listener: StateEventToken,
        revision: Int,
        actions: List<ServerDrivenNode>,
    ) {
        if (revision != onStateUpdated.value) return
        val origins = if (revision == mutationRevision) mutationListeners else emptySet()
        if (listener in origins) return
        if (origins.size >= 64) {
            setError(ErrorData.taskError("State event chain limit exceeded").copy(
                diagnostic = NodeDiagnostic("EVENT_CHAIN_LIMIT", "State event chain limit exceeded", "stateEvent"),
            ))
            return
        }
        withContext(StateMutationOrigin(origins + listener)) { executeActions(actions) }
    }

    fun resolveStateKeys(
        value: String?
    ): Array<String> {
        val text = value ?: return emptyArray()
        if (value.contains("#{")) {
            val placeholderMatches = Regex("#\\{([^}]+)\\}").findAll(text)
            return placeholderMatches.map { it.groupValues[1] }.toList().toTypedArray()
        }
        return emptyArray()
    }

    /**
     * Resolves state placeholders within a given string.
     *
     * This function searches for placeholders in the format `#{key}` within the input string `value`.
     * For each placeholder found, it replaces the placeholder (e.g., `#{user_name}`) with the corresponding
     * value from the `formState` map. If a key is not found in the map, it is added with an
     * empty string, and that empty string is used for the replacement.
     *
     * @param value The string containing placeholders to be resolved. Can be null.
     * @return A new string with all placeholders replaced by their corresponding state values,
     *         or `null` if the input `value` is `null`.
     */
    fun resolveStatePlaceholders(
        value: String?
    ): String {
        var text = value ?: return ""
        if (value.contains("#{")) {
            val placeholderMatches = Regex("#\\{([^}]+)\\}").findAll(text)
            for (match in placeholderMatches) {
                val key = match.groupValues[1]
                val valueFromState = _formState[key] ?: run {
                    _formState[key] = ""
                    ""
                }
                text = text.replace(match.value, valueFromState)
            }
        }
        return text
    }

    /**
     * Creates a [MutableStateFlow] that emits the resolved value of the input string with state placeholders.
     *
     * This function takes a string `value` that may contain state placeholders in the format `#{key}`.
     * It creates a [MutableStateFlow] that initially emits the resolved value of the input string,
     * where all placeholders are replaced with their corresponding values from the `formState` map.
     *
     * If the input string contains any placeholders, the function sets up a coroutine to listen for
     * updates to the state. Whenever the state is updated (indicated by changes in `onStateUpdated`),
     * it re-evaluates the input string and updates the flow's value if it has changed.
     *
     * @param value The string containing placeholders to be resolved. Can be null.
     * @return A [MutableStateFlow] emitting the resolved string with all placeholders replaced
     *         by their corresponding state values.
     */
    fun resolveStatePlaceholdersFlow(
        value: String?,
    ) = MutableStateFlow(resolveStatePlaceholders(value)).also { flow ->
        if (value?.contains("#{") == true) {
            legacyBindings[flow] = viewModelScope.launch {
                onStateUpdated.collect { _ ->
                    val valueResolve = resolveStatePlaceholders(value)
                    if (flow.value != valueResolve) {
                        flow.value = valueResolve
                    }
                }
            }
        }
    }

    /**
     * Creates a [MutableStateFlow] that emits a calculated value based on the resolved state placeholders in the input string.
     *
     * This function takes a string `value` that may contain state placeholders in the format `#{key}`,
     * and a `calculation` function that transforms the resolved string into a value of type `T`.
     * It creates a [MutableStateFlow] that initially emits the result of applying the `calculation`
     * function to the resolved value of the input string.
     *
     * If the input string contains any placeholders, the function sets up a coroutine to listen for
     * updates to the state. Whenever the state is updated (indicated by changes in `onStateUpdated`),
     * it re-evaluates the input string, applies the `calculation` function, and updates the flow's
     * value if it has changed.
     *
     * @param value The string containing placeholders to be resolved. Can be null.
     * @param calculation A function that takes the resolved string and returns a value of type `T`.
     * @return A [MutableStateFlow] emitting values of type `T` calculated from the resolved string
     *         with all placeholders replaced by their corresponding state values.
     */
    fun <T> resolveStatePlaceholdersFlow(
        value: String?,
        calculation: (String?) -> T,
    ) = MutableStateFlow(calculation(resolveStatePlaceholders(value))).also { flow ->
        if (value?.contains("#{") == true) {
            legacyBindings[flow] = viewModelScope.launch {
                onStateUpdated.collect { _ ->
                    val valueResolve = resolveStatePlaceholders(value)
                    if (flow.value != valueResolve) {
                        flow.value = calculation(valueResolve)
                    }
                }
            }
        }
    }

    /**
     * Loads a NodeProvider based on the given node type.
     *
     * This function attempts to retrieve a NodeProvider from the `nodeTypeProviders` map
     * using the provided `nodeType` key. If the provider is found, it is returned.
     * If not found, an error message is printed, an error state is set using `setError`,
     * and `null` is returned.
     *
     * @param nodeType The type of the node for which to load the provider.
     * @return The corresponding NodeProvider if found, or `null` if not found.
     */
    fun loadNodeTypeProvider(
        nodeType: String
    ): NodeProvider? {
        return nodeTypeProviders[nodeType].also { provider ->
            if (provider == null) setError(ErrorData.missingProvider(nodeType).copy(
                diagnostic = NodeDiagnostic("MISSING_PROVIDER", "Provider is not registered", "resolveProvider"),
            ))
        }
    }

    /**
     * Loads a ComposableContent component based on the given node type and destiny.
     *
     * This suspend function first increments the `_runningTasks` counter to indicate that a task is in progress.
     * It then attempts to load a NodeProvider using the `loadNodeTypeProvider` function with the provided
     * `nodeType`. If a provider is found, it invokes the provider with the `destiny` parameter to get a
     * ServerDrivenNode, which is then passed to the `loadComponent` function to obtain the corresponding
     * ComposableContent component.
     *
     * If no provider is found, an error message is logged, an error state is set using `setError`,
     * and `null` is returned. If any exception occurs during this process, it is caught, logged,
     * an error state is set, and `null` is returned.
     *
     * Finally, regardless of success or failure, the `_runningTasks` counter is decremented to indicate
     * that the task has completed.
     *
     * @param nodeType The type of the node for which to load the component.
     * @param destiny The destiny parameter used by the NodeProvider to determine the specific layout or configuration.
     * @return The corresponding ComposableContent component if successfully loaded, or `null` if not found or an error occurred.
     */
    suspend fun loadComponent(nodeType: String, destiny: String): ComposableContent? {
        _runningTasks.update { it + 1 }
        return try {
            val provider = loadNodeTypeProvider(nodeType) ?: return null
            loadComponent(provider(destiny))
        } catch (e: CancellationException) {
            throw e
        } catch (e: PayloadException) {
            setError(ErrorData.payload(e))
            null
        } catch (e: Exception) {
            setError(ErrorData.providerError(nodeType).copy(
                diagnostic = NodeDiagnostic("PROVIDER_ERROR", "Provider failed", "provider", documentId = destiny),
                cause = e,
            ))
            null
        } finally {
            _runningTasks.update { it - 1 }
        }
    }

    /**
     * Registers a NodeProvider for a specific node type.
     *
     * This function adds the provided `handler` to the `nodeTypeProviders` map,
     * associating it with the specified `nodeType`. This allows the ViewModel to
     * later retrieve and use the appropriate NodeProvider when loading components
     * of that type.
     *
     * @param nodeType The type of the node for which to register the provider.
     * @param handler The NodeProvider to be associated with the specified node type.
     */
    fun addNodeProvider(
        nodeType: String, handler: NodeProvider
    ) {
        nodeTypeProviders[nodeType] = handler
    }

    /**
     * Registers a library of components and actions under a specific namespace.
     *
     * This function adds the provided `library` to the `libraries` map,
     * associating it with its `namespace`. This allows the ViewModel to
     * later retrieve and use the components and actions defined in that library
     * when loading and invoking them.
     *
     * @param library The SDLibrary containing components and actions to be registered.
     */
    fun addLibrary(library: SDLibrary) {
        libraries[library.namespace] = library
    }

    private fun getComponent(node: ServerDrivenNode): ComponentHandler? {
        val (libraryNamespace, componentNamespace) = node.qualifiedName()
        validateDefinition(node, NodeRole.COMPONENT)
        val library = libraries[libraryNamespace]
        return library?.getComponent(componentNamespace)
    }

    /**
     * Loads a ComposableContent component based on the provided ServerDrivenNode.
     *
     * This function retrieves the component identifier from the given `node` and
     * attempts to find the corresponding ComponentHandler using the `getComponent` method.
     * If a handler is found, it invokes the handler with the `node` and the current
     * ViewModel instance to obtain the ComposableContent component.
     *
     * If no handler is found, an error message is logged, an error state is set using
     * `setError`, and `null` is returned. If any exception occurs during this process,
     * it is caught, logged, an error state is set, and `null` is returned.
     *
     * @param node The ServerDrivenNode containing the component information to be loaded.
     * @return The corresponding ComposableContent component if successfully loaded,
     *         or `null` if not found or an error occurred.
     */
    fun loadComponent(node: ServerDrivenNode): ComposableContent? {
        val nodeComponent = node.component // button? text? topbar? column?
        try {
            val component = getComponent(node)
            return component?.invoke(node, this) ?: run {
                logging.error("Component not found for: $nodeComponent")
                setError(ErrorData.missingComponent(nodeComponent).copy(
                    diagnostic = node.diagnostic("MISSING_COMPONENT", "Component is not registered", "resolve", "${node.sourcePath}.component"),
                ))
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: PayloadException) {
            setError(ErrorData.payload(e))
            return null
        } catch (e: Exception) {
            setError(ErrorData.componentError(nodeComponent).copy(
                diagnostic = node.diagnostic("HANDLER_FAILURE", "Component handler failed", "componentHandler"),
                cause = e,
            ))
            return null
        }
    }

    private fun getAction(
        node: ServerDrivenNode
    ): ActionHandler? {
        val (libraryNamespace, componentNamespace) = node.qualifiedName()
        validateDefinition(node, NodeRole.ACTION)
        val library = libraries[libraryNamespace]
        return library?.getAction(componentNamespace)
    }

    /**
     * Invokes a list of actions defined by ServerDrivenNode instances.
     *
     * This function takes a list of `actions` and executes each action sequentially
     * within a coroutine scope. It provides optional `beforeInvoke` and `afterInvoke`
     * suspend functions that are executed before and after the action invocations, respectively.
     *
     * For each action, it retrieves the action identifier and attempts to find the corresponding
     * ActionHandler using the `getAction` method. If a handler is found, it invokes the handler
     * with the action node and the current ViewModel instance.
     *
     * If no handler is found for an action, an error message is logged, and an error state is set
     * using `setError`. If any exception occurs during the invocation of an action, it is caught,
     * logged, and an error state is set.
     *
     * The `_runningTasks` counter is incremented at the start of the invocation process and decremented
     * at the end to indicate that tasks are in progress.
     *
     * @param actions A list of ServerDrivenNode instances representing the actions to be invoked.
     * @param beforeInvoke An optional suspend function to be executed before invoking the actions.
     * @param afterInvoke An optional suspend function to be executed after invoking the actions.
     */
    private val actionExecutor = ActionExecutor(::getAction, this)

    fun catalog(): NodeCatalog = NodeCatalog(libraries.values.flatMap { it.definitions })

    private fun validateDefinition(node: ServerDrivenNode, role: NodeRole) {
        catalog().validate(node, role).firstOrNull { it.severity == DiagnosticSeverity.ERROR }
            ?.let { throw PayloadException(it) }
    }

    suspend fun executeActions(
        actions: List<ServerDrivenNode>,
        policy: ActionFailurePolicy? = null,
        limits: ActionExecutionLimits? = null,
    ): ActionExecutionResult = withContext(Dispatchers.Main.immediate) {
        withRunningTask { actionExecutor.execute(actions, policy, limits) }
    }

    fun invokeActions(
        actions: ArrayList<ServerDrivenNode>,
        beforeInvoke: suspend CoroutineScope.() -> Unit = {},
        afterInvoke: suspend CoroutineScope.() -> Unit = {},
    ): Job = viewModelScope.launch {
        val scope = this
        withRunningTask {
            var primary: Throwable? = null
            var reported: ErrorData? = null
            try {
                beforeInvoke(scope)
                reported = executeActions(actions).errors.firstOrNull()
            } catch (error: Throwable) {
                primary = error
                if (error is CancellationException || error !is Exception) throw error
                setError(ErrorData.taskError("Action callback failed").copy(cause = error,
                    diagnostic = NodeDiagnostic("CALLBACK_FAILURE", "Action callback failed", "beforeInvoke")))
            } finally {
                try {
                    withContext(NonCancellable) { afterInvoke(this) }
                } catch (cleanup: Throwable) {
                    val earlier = primary
                    val earlierReport = reported
                    when {
                        cleanup !is Exception -> {
                            if (earlier != null) cleanup.addSuppressed(earlier)
                            throw cleanup
                        }
                        cleanup is CancellationException && earlier !is CancellationException -> {
                            if (earlier != null) cleanup.addSuppressed(earlier)
                            throw cleanup
                        }
                        earlier != null -> earlier.addSuppressed(cleanup)
                        earlierReport != null -> {
                            val cause = earlierReport.cause ?: IllegalStateException("Action execution failed")
                            cause.addSuppressed(cleanup)
                            setError(earlierReport.copy(cause = cause))
                        }
                        else -> setError(ErrorData.taskError("Action cleanup failed").copy(cause = cleanup,
                            diagnostic = NodeDiagnostic("CALLBACK_FAILURE", "Action cleanup failed", "afterInvoke")))
                    }
                }
            }
        }
    }
}
