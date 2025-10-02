package me.developes.humming.sdui.common

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.StateFactoryMarker
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SDUIFormViewModel(
    private val handle: SavedStateHandle,
    libraries: List<SDLibrary> = listOf()
) : ViewModel() {
    private val _runningTasks = MutableStateFlow(0)
    val isLoading = derivedStateOf { _runningTasks.value > 0 }.let { derivedState ->
        MutableStateFlow(derivedState.value).also { flow ->
            viewModelScope.launch {
                _runningTasks.collect { value ->
                    flow.value = value > 0
                }
            }
        }
    }.asStateFlow()

    private val _errorState = MutableStateFlow<ErrorData?>(null)
    val errorState: StateFlow<ErrorData?> = _errorState.asStateFlow()

    private val _formState = MutableStateFlow<MutableMap<String, String?>>(mutableMapOf())

    private val nodeTypeProviders: HashMap<String, NodeProvider> = HashMap()
    private val libraries: HashMap<String, SDLibrary> = HashMap<String, SDLibrary>().apply {
        libraries.forEach { library ->
            this[library.namespace] = library
        }
    }

    fun runTask(block: suspend () -> Unit, onError: (Throwable) -> ErrorData? = { null }) {
        viewModelScope.launch {
            _runningTasks.value++
            try {
                block()
            } catch (e: Throwable) {
                setError(onError(e) ?: ErrorData.taskError(e.message ?: ""))
            }
            _runningTasks.value--
        }
    }

    fun setError(error: ErrorData) {
        _errorState.value = error
    }

    fun dismissError() {
        _errorState.value = null
    }

    fun updateState(key: String, value: String?) {
        _formState.update { currentMap ->
            val newMap = currentMap.toMutableMap()
            if (value.isNullOrBlank()) {
                newMap.remove(key)
            } else {
                newMap[key] = value
            }
            newMap
        }
    }

    /**
     * retrieves a string value from a map based on a provided key,
     * then uses a provided calculation function to transform that string into a state value of the desired type.
     *
     * @see [StateFactoryMarker]: This annotation indicates that the function is a "state factory". It's used internally by Compose to improve the performance of state management.
     *
     * @param key: [String]: This parameter represents a unique key used to identify the state within the stateMap.
     * @param calculation: ([String]?) -> [T]: This parameter is a lambda function that takes a nullable string as input and returns a value of type [T]. This function is used to compute the state value based on the string retrieved from the stateMap.
     * @see derivedStateOf { ... }: This creates a derived state. Derived state means that this state's value is calculated based on other state values. In this case, it depends on the value returned by propertyState(key, stateMap).
     * @return [derivedStateOf] calculation.invoke(text): This line calculates the final state value by invoking the calculation lambda function with the text retrieved from the stateMap.
     * **/
    @StateFactoryMarker
    fun <T> propertyState(
        key: String,
        calculation: (String?) -> T,
    ): State<T> = derivedStateOf {
        val text = propertyState(key)
        calculation(text.value)
    }

    /**
     * retrieves a string value from a map based on a provided key,
     * then returns it as a state value.
     *
     * @see [StateFactoryMarker]: This annotation indicates that the function is a "state factory". It's used internally by Compose to improve the performance of state management.
     *
     * @param key: [String]: This parameter represents a unique key used to identify the state within the stateMap.
     * @return [derivedStateOf] text: This line calculates the final state value by invoking the calculation lambda function with the text retrieved from the stateMap.
     * **/
    @StateFactoryMarker
    fun propertyState(
        key: String, default: String? = null
    ): State<String?> = derivedStateOf {
        _formState.value[key]?.let {
            var text = it
            val keys = Regex("#\\{([^}]+)\\}").findAll(text)
            for (occurrence in keys) {
                val stateKey = occurrence.groupValues[1]
                val value = _formState.value[stateKey] ?: run {
                    _formState.value[stateKey] = ""
                    ""
                }
                text = text.replace(occurrence.value, value)
            }
            text
        } ?: default
    }

    /**
     * retrieves a [NodeProvider] for a given node type.
     *
     * It takes a nodeType string as an argument.
     * It looks up the nodeType in a map called [nodeTypeProviders].
     * If a NodeProvider is found for the nodeType, it is returned.
     * If no provider is found, it throws an IllegalStateException with a message indicating that no provider exists for the given type.
     * */
    fun loadNodeTypeProvider(
        nodeType: String
    ): NodeProvider? {
        return try {
            nodeTypeProviders[nodeType]!!
        } catch (e: Exception) {
            println(
                "Server Driven Node Provider not found: $nodeType\n" +
                        "Message: ${e.message}\n" +
                        "StackTrace: ${e.stackTraceToString()}"
            )
            setError(ErrorData.missingProvider(nodeType))
            null
        }
    }

    suspend fun loadComponent(nodeType: String, destiny: String): ComposableContent? {
        _runningTasks.value++
        return try {
            loadNodeTypeProvider(nodeType)?.invoke(destiny)?.let {
                loadComponent(it)
            }
        } catch (e: Exception) {
            println(
                "Server Driven Component Node Provider not found: $nodeType\n" +
                        "Message: ${e.message}\n" +
                        "StackTrace: ${e.stackTraceToString()}"
            )
            setError(ErrorData.missingProvider(nodeType))
            null
        } finally {
            _runningTasks.value--
        }
    }

    fun addNodeProvider(
        nodeType: String, handler: NodeProvider
    ) {
        nodeTypeProviders[nodeType] = handler
    }

    fun addLibrary(library: SDLibrary) {
        libraries[library.namespace] = library
    }

    private fun getComponent(nodeComponent: String): ComponentHandler? {
        val split = nodeComponent.split(':')
        val libraryNamespace = split[0]
        val componentNamespace = split[1]
        val library = libraries[libraryNamespace]
        return library?.getComponent(componentNamespace)
    }

    fun loadComponent(node: ServerDrivenNode): ComposableContent? {
        val nodeComponent = node.component // button? text? topbar? column?
        val component = getComponent(nodeComponent)
        try {
            return component!!.invoke(node, this)
        } catch (e: Exception) {
            println(
                "Server Driven Component not found: $nodeComponent\n" +
                        "Message: ${e.message}\n" +
                        "StackTrace: ${e.stackTraceToString()}"
            )
            setError(ErrorData.missingComponent(nodeComponent))
            return null
        }
    }

    private fun getAction(
        nodeAction: String
    ): ActionHandler? {
        val split = nodeAction.split(':')
        val libraryNamespace = split[0]
        val componentNamespace = split[1]
        val library = libraries[libraryNamespace]
        return library?.getAction(componentNamespace)
    }

    fun invokeActions(
        actions: ArrayList<ServerDrivenNode>,
        beforeInvoke: suspend CoroutineScope.() -> Unit = {},
        afterInvoke: suspend CoroutineScope.() -> Unit = {},
    ) {
        viewModelScope.launch {
            _runningTasks.value++
            beforeInvoke()
            actions.forEach { action ->
                try {
                    val nodeComponent = action.component
                    val handler = getAction(nodeComponent)
                    if (handler != null) {
                        handler.invoke(action, this@SDUIFormViewModel)
                    } else {
                        println("Action not found for component: $nodeComponent")
                        setError(ErrorData.missingAction(nodeComponent))
                    }
                } catch (e: Throwable) {
                    println(
                        "Error invoking action for component: ${action.component}\n" +
                                "Message: ${e.message}\n" +
                                "StackTrace: ${e.stackTraceToString()}"
                    )
                    setError(ErrorData.actionError(action.component, e.message ?: ""))
                }
            }
            afterInvoke()
            _runningTasks.value--
        }
    }
}