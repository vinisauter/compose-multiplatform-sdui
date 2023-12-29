package me.next.serverdriven.core.library.navigation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import generateUUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.tree.IgnoredNode
import me.next.serverdriven.core.tree.ServerDrivenNode

var LocalNavigator: SDCNavigator? = null

typealias Route = String

val LocalDestinationId: ProvidableCompositionLocal<String> = staticCompositionLocalOf {
    generateUUID()
}

class SDCNavigator(
    graphNode: ServerDrivenNode
) {
    private lateinit var startNode: Route
    private val routes: HashMap<String, ServerDrivenNode> = HashMap()
    private val links: HashMap<String, ServerDrivenNode> = HashMap()
    val states: SnapshotStateMap<String, String> by derivedStateOf {
        mutableStateMapOf(
            *graphNode.propertyMap("states")
                .mapValues { it.value ?: "" }.toList().toTypedArray()
        )
    }

    init {
        val startDestination = graphNode.property("startDestination")!!
        graphNode.children?.let {
            for (serverDrivenNode in it) {
                if (serverDrivenNode.component == "navigation:node") {
                    val name = serverDrivenNode.property("name")!!
                    routes[name] = serverDrivenNode
                    if (startDestination == name) {
                        startNode = name
                    }
                    serverDrivenNode.property("link")?.let { link ->
                        links[link] = serverDrivenNode
                    }
                }
            }
        }
        if (LocalNavigator == null) {
            LocalNavigator = this
        } else {
            LocalNavigator?.let {
                it.routes.putAll(routes)
                it.links.putAll(links)
                it.states.putAll(states)
            }
        }
    }

    private val _backStack = MutableStateFlow(listOf(startNode))
    val backStack: StateFlow<List<Route>> = _backStack.asStateFlow()
    private val _nodeFlow: MutableStateFlow<ServerDrivenNode> = MutableStateFlow(
        runBlocking { loadRouteNode(startNode) }
    )
    val nodeFlow = _nodeFlow.asStateFlow()

    fun handleLink(link: String) {
        //TODO: handleLink
    }

    suspend fun navigateTo(routeName: Route) {
        val graphNode = routes[routeName]!!
        graphNode.property("type")!!
        graphNode.property("destiny")!!

        val routeNode = loadRouteNode(routeName)
        if (routeNode is IgnoredNode) return

        if (_backStack.value.last() == routeName) return

        _backStack.update { currentState ->
            currentState + routeName
        }
        _nodeFlow.update {
            routeNode
        }
    }

    suspend fun navigateBack() {
        if (_backStack.value.size > 1) {
            _backStack.update { currentState ->
                currentState.dropLast(1)
            }
            val route: Route = _backStack.value.lastOrNull() ?: return
            val routeNode = loadRouteNode(route)
            if (routeNode is IgnoredNode) return
            _nodeFlow.update {
                routeNode
            }
        } else {
            // TODO Close Screen
        }
    }

    suspend fun loadRouteNode(routeName: Route): ServerDrivenNode {
        val graphNode = routes[routeName]!!
        val nodeType = graphNode.property("type")!!
        val layout = graphNode.property("destiny")!!
        return SDCLibrary.loadNodeTypeProvider(nodeType).invoke(layout)
    }
}

@Composable
fun NavigationHost(
    navigator: SDCNavigator
) {
    val backStack: List<Route> by navigator.backStack.collectAsState()
    val previousBackStackSize: Int by remember { mutableStateOf(backStack.size) }
    val isForwardNavigation: Boolean by derivedStateOf { backStack.size > previousBackStackSize }
    val node by navigator.nodeFlow.collectAsState()

    AnimatedContent(
        targetState = node,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            if (isForwardNavigation) {
                slideInHorizontally(initialOffsetX = { width -> width }) togetherWith
                        slideOutHorizontally(targetOffsetX = { width -> -width })
            } else {
                slideInHorizontally(initialOffsetX = { width -> -width }) togetherWith
                        slideOutHorizontally(targetOffsetX = { width -> width })
            }
        }
    ) {
        val screen = SDCNavigationScreen(screenNode = it, states = navigator.states)
        NavigationDestination {
            screen.Content()
        }
    }
}

@Composable
fun NavigationDestination(
    content: @Composable () -> Unit
) {
    val destinationId: String = rememberSaveable { generateUUID() }
    CompositionLocalProvider(LocalDestinationId provides destinationId) {
        content()
    }
}