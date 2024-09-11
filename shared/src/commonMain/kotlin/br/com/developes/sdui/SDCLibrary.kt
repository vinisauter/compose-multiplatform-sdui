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

class SDCLibrary private constructor() {
    private val libraries: HashMap<String, SDLibrary> = HashMap()

    companion object {
        private val _isLoading: MutableStateFlow<Boolean> =
            MutableStateFlow(false)
        val isLoading = _isLoading.asStateFlow()

        private val _hasError: MutableStateFlow<Throwable?> = MutableStateFlow(null)
        val hasError = _hasError.asStateFlow()

        private val LocalNodeTypeProviders: HashMap<String, NodeProvider> = HashMap()
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

        fun registerNodeTypeProvider(
            nodeType: String,
            handler: NodeProvider
        ) {
            LocalNodeTypeProviders[nodeType] = handler
        }

        fun loadNodeTypeProvider(
            nodeType: String
        ): NodeProvider {
            return LocalNodeTypeProviders[nodeType] ?: error("No NodeProvider for type: $nodeType")
        }

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