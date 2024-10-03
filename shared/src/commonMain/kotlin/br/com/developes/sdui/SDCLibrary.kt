package br.com.developes.sdui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.developes.sdui.action.SDAction
import br.com.developes.sdui.layout.SDLayout
import br.com.developes.sdui.navigation.SDNavigation
import br.com.developes.sdui.provider.components.JsonFileNodeTypeProvider
import br.com.developes.sdui.provider.components.JsonNodeTypeProvider
import br.com.developes.sdui.utils.AnimatedDialog
import br.com.developes.sdui.utils.SimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * provides a way to manage the loading of external SDK libraries in your Android application. It handles adding libraries, observing loading and error states,
 * and displaying appropriate dialogs based on those states.
 *
 * @param libraries: SDLibrary: This parameter allows you to pass a variable number of SDLibrary objects to the function.
 * @param debug: Boolean = false: This optional parameter controls whether to show debug information (states) or not. It defaults to false.
 * @param block: @Composable SDCLibrary.() -> Unit: This is a lambda function that takes an SDCLibrary instance as its receiver and doesn't return anything (Unit).
 * This block is where you would define the Composables that use the loaded libraries.
 *
 * @property show_states = debug: This line likely sets a global variable or property named show_states to the value of the debug parameter.
 * This could be used elsewhere in the code to conditionally display debug information.
 *
 * SDCLibrary.instance.apply {...}: This gets the singleton instance of the SDCLibrary class and uses the apply scope function to configure it.
 * Inside the apply block, the provided libraries (libraries vararg) are added to the SDCLibrary instance using the addLibrary function.
 * */
@Composable
fun SDCLibrary(
    vararg libraries: SDLibrary,
    debug: Boolean = false,
    block: @Composable SDCLibrary.() -> Unit
) {
    show_states = debug
    block.invoke(SDCLibrary.instance.apply {
        for (library in libraries) {
            addLibrary(library)
        }
    })

    val loading by SDCLibrary.isLoading.collectAsState()
    val error by SDCLibrary.hasError.collectAsState()
    val hasError by derivedStateOf { error != null }

    if (loading) {
        LoadingDialog()
    }
    if (hasError) {
        error?.let { ErrorDialog(it) }
    }
}

var show_states: Boolean = false
val logger = SimpleLogger("server-driven")
typealias NodeProvider = suspend (String) -> ServerDrivenNode

/**
 * class designed to manage and load server-driven components.
 *
 * @property libraries: HashMap<String, SDLibrary>: A map to store instances of other SDLibrary objects,
 * representing different modules or feature sets,
 * indexed by a namespace (a string identifier).
 *
 * Here's a breakdown of its key aspects:
 * */
class SDCLibrary private constructor() {
    private val libraries: HashMap<String, SDLibrary> = HashMap()

    /**
     * State Handling:
     * @property _isLoading, isLoading: Uses MutableStateFlow and asStateFlow to manage and
     * expose a state indicating whether the library is currently loading something (probably components from a server).
     * @property _hasError, hasError: Similar to isLoading,
     * this manages and exposes a state holding a potential error encountered during loading.
     *
     * Component and Node Type Providers:
     * @property LocalNodeTypeProviders: A map to store functions ([NodeProvider]) that handle different node types (e.g., "json", "file").
     * These providers parse and process different data formats received from the server.
     * @property LocalLib: A CompositionLocal that provides an instance of SDCLibrary. [CompositionLocal] allow you to pass data down the Compose component tree implicitly.
     * [instance]: A [Composable] property to access the current [SDCLibrary] instance from the [LocalLib].
     * [registerNodeTypeProvider]: Registers a [NodeProvider] for a specific node type.
     * [loadNodeTypeProvider]: Retrieves the [NodeProvider] for a given node type.
     * */
    companion object {
        private val _isLoading: MutableStateFlow<Boolean> =
            MutableStateFlow(false)
        val isLoading = _isLoading.asStateFlow()

        private val _hasError: MutableStateFlow<Throwable?> = MutableStateFlow(null)
        val hasError = _hasError.asStateFlow()

        private val LocalNodeTypeProviders: HashMap<String, NodeProvider> = HashMap()


        /**
         * sets up a CompositionLocal named [LocalLib] that holds an initialized [SDCLibrary] instance.
         * This library comes pre-configured with default libraries for layout, actions, and navigation,
         * as well as node type providers for handling various data types.
         * By making this library available through a CompositionLocal,
         * any composable function within your application can easily access its functionalities
         * and the provided libraries without needing them to be explicitly passed as parameters.
         * */
        private val LocalLib = staticCompositionLocalOf {
            val defaultLibraries: List<SDLibrary> = listOf(
                SDLayout(),
                SDAction().also {
                    it.registerMethod("getPlatformName") { node, states ->
                        val stateName = node.property("state") ?: "platformName"
                        states[stateName] = getPlatform().name
                    }
                },
                SDNavigation()
            )
            SDCLibrary().apply {
                registerNodeTypeProvider("json") { json ->
                    JsonNodeTypeProvider(json).node
                }
                registerNodeTypeProvider("file") { res ->
                    JsonFileNodeTypeProvider(res).node
                }
                registerNodeTypeProvider("link") { link ->
                    openUrl(link)
                    IgnoredNode
                }
                for (library in defaultLibraries) {
                    addLibrary(library)
                }
            }
        }
        val instance: SDCLibrary
            @Composable
            get() = LocalLib.current

        /**
         * This function registers a custom node type provider.
         * It takes two arguments:
         * @param nodeType: A [String] specifying a name to a node type.
         * @param handler: A [NodeProvider] instance that will handle the creation of nodes for the specified type.
         * The function stores the handler in a [LocalNodeTypeProviders] object,
         * associating it with the given nodeType.
         * This allows the system to look up the correct provider when a node of the specified type needs to be created.
         * This pattern is commonly used in systems that support plugins or extensions,
         * where custom node types can be added at runtime.
         * */
        fun registerNodeTypeProvider(
            nodeType: String,
            handler: NodeProvider
        ) {
            LocalNodeTypeProviders[nodeType] = handler
        }

        /**
         * retrieves a [NodeProvider] for a given node type.
         *
         * It takes a nodeType string as an argument.
         * It looks up the nodeType in a map called [LocalNodeTypeProviders].
         * If a NodeProvider is found for the nodeType, it is returned.
         * If no provider is found, it throws an IllegalStateException with a message indicating that no provider exists for the given type.
         * */
        fun loadNodeTypeProvider(
            nodeType: String
        ): NodeProvider {
            return LocalNodeTypeProviders[nodeType] ?: error("No NodeProvider for type: $nodeType")
        }

        /**
         * responsible for dynamically rendering UI components based on data received from a server.
         * step by step:
         *
         * @param node: A [ServerDrivenNode] object that presumably holds information about the component to be rendered,
         * obtained from a server response.
         * @param dataState: A [MutableMap] used to store and manage state related to the UI.
         *
         * step by step:
         * if (node is IgnoredNode) return: This line checks if the node is an instance of IgnoredNode.
         * If it is, the function returns without rendering anything. This suggests that certain nodes might be marked as ignorable in the server response.
         *
         * val nodeComponent = node.component: This retrieves the actual component identifier (likely a string) from the node object.
         *
         * val component = instance.getComponent(nodeComponent): This line retrieves a Composable function associated with the nodeComponent identifier.
         * It seems like instance is an object (possibly a class or singleton) that holds a registry of available components.
         *
         * if (component != null) { ... } else { ... }: This conditional block checks if a corresponding Composable function was found for the given nodeComponent.
         * component.invoke(node, dataState): If a component is found, it's invoked (called) and passed the node and dataState as arguments.
         * This is how the actual UI component is rendered.
         * Text(...): If no component is found, an error message is displayed within a Text composable.
         * This message indicates that the server-driven component is unknown and includes the unrecognized node.component for debugging purposes.
         * */
        @Composable
        fun loadComponent(
            node: ServerDrivenNode,
            dataState: MutableMap<String, String>
        ) {
            if (node is IgnoredNode) return
            val nodeComponent = node.component
            val component = instance.getComponent(nodeComponent)
            if (component != null) {
                component.invoke(node, dataState)
            } else {
                Text(
                    text = "UNKNOWN SERVER DRIVEN COMPONENT\n${node.component}",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Red)
                        .wrapContentSize()
                )
            }
        }


        /**
         * this function tries to load an action based on a server-driven node.
         * If successful, it returns the action; otherwise,
         * it displays an error message in the UI and returns a no-op action.
         * */
        @Composable
        fun loadAction(node: ServerDrivenNode?): ActionHandler {
            val nodeComponent = node?.component ?: return { _, _ -> }
            val action = instance.getAction(nodeComponent)
            if (action != null) {
                return action
            } else {
                Text(
                    text = "UNKNOWN SERVER DRIVEN ACTION\n${node.component}",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Color.Red)
                        .wrapContentSize()
                )
                return { _, _ -> }
            }
        }

        /**
         * processes a list of ServerDrivenNode objects to create a combined ActionHandler.
         *
         * @property actions = ArrayList<Pair<ServerDrivenNode, ActionHandler>>(). An ArrayList is created to store pairs of ServerDrivenNode and ActionHandler.
         *
         * ```for (node in nodes)```: The code iterates through each ServerDrivenNode in the input nodes list.
         *
         * ```actions.add(Pair(node, loadAction(node)))```: For each node, loadAction(node) is called (another function that returns an ActionHandler for the specific node).
         *
         * The node and its corresponding [ActionHandler] are added as a pair to the actions list.
         * */
        @Composable
        fun loadActions(nodes: ArrayList<ServerDrivenNode>): ActionHandler {
            val actions = ArrayList<Pair<ServerDrivenNode, ActionHandler>>()
            for (node in nodes) {
                actions.add(Pair(node, loadAction(node)))
            }
            return { _, map ->
                for (action in actions) {
                    action.second.invoke(action.first, map)
                }
            }
        }

        fun onErrorHandled() = _hasError.update { null }
        fun onError(error: Throwable) {
            _hasError.update {
                error
            }
        }

        fun onLoading(isLoading: Boolean) {
            _isLoading.update {
                isLoading
            }
        }

        fun CoroutineScope.launchHandling(
            context: CoroutineContext = EmptyCoroutineContext,
            start: CoroutineStart = CoroutineStart.DEFAULT,
            after: suspend CoroutineScope.() -> Unit = {},
            block: suspend CoroutineScope.() -> Unit
        ): Job = launch(context, start) {
            onLoading(true)
            try {
                block.invoke(this)
            } catch (error: Throwable) {
                logger.e("LaunchHandling", error)
                onError(error)
            } finally {
                onLoading(false)
                after()
            }
        }
    }

    fun addLibrary(library: SDLibrary): SDCLibrary {
        if (libraries[library.namespace] == null) {
            libraries[library.namespace] = library
        } else {
            libraries[library.namespace]?.merge(library)
        }
        return this
    }

    fun getAction(
        nodeAction: String
    ): ActionHandler? {
        val split = nodeAction.split(':')
        val libraryNamespace = split[0]
        val componentNamespace = split[1]
        val library = libraries[libraryNamespace]
        return library?.getAction(componentNamespace)
    }

    fun getComponent(nodeComponent: String): ComponentHandler? {
        val split = nodeComponent.split(':')
        val libraryNamespace = split[0]
        val componentNamespace = split[1]
        val library = libraries[libraryNamespace]
        return library?.getComponent(componentNamespace)
    }
}

@Composable
internal fun LoadingDialog() {
    AnimatedDialog(
        onDismissRequest = {},
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
    ) { _ ->
        CircularProgressIndicator(color = MaterialTheme.colors.background)
    }
}

@Composable
fun ErrorDialog(
    error: Throwable
) {
    AnimatedDialog(
        onDismissRequest = {
            SDCLibrary.onErrorHandled()
        },
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
    ) { transitionDialogHelper ->
        val scope = rememberCoroutineScope()
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Error: ${
                        error.message ?: error.cause?.message ?: error::class.qualifiedName
                    }"
                )
                Button(onClick = {
                    transitionDialogHelper.triggerAnimatedDismiss()
                }) {
                    Text("Close")
                }
            }
        }
    }
}