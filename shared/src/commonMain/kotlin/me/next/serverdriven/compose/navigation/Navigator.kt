package me.next.serverdriven.compose.navigation

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
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.produceUiState
import me.next.serverdriven.compose.provider.SDCLoaderLayout
import me.next.serverdriven.core.library.SDCNavigationScreen
import me.next.serverdriven.core.tree.IgnoredNode
import me.next.serverdriven.core.tree.ServerDrivenNode

var LocalNavigator: Navigator? = null

typealias Route = String

val LocalDestinationId: ProvidableCompositionLocal<String> = staticCompositionLocalOf {
    generateUUID()
}

class Navigator(
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

    fun handleLink(link: String) {
        //TODO: handleLink
    }

    fun navigateTo(route: Route) {
        val graphNode = routes[route]!!
        val nodeType = graphNode.property("type")!!
        val layout = graphNode.property("destiny")!!

        val routeNode = loadRouteNode(route)
        if (routeNode is IgnoredNode) return

        if (_backStack.value.last() == route) return

        _backStack.update { currentState ->
            currentState + route
        }
    }

    fun navigateBack() {
        if (_backStack.value.size > 1) {
            _backStack.update { currentState ->
                currentState.dropLast(1)
            }
        } else {
            // TODO Close Screen
        }
    }

    fun loadRouteNode(route: Route): ServerDrivenNode {
        val graphNode = routes[route]!!
        val nodeType = graphNode.property("type")!!
        val layout = graphNode.property("destiny")!!
        return SDCLibrary.loadNodeTypeProvider(nodeType).invoke(layout)
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

@Composable
fun NavigationHost(
    navigator: Navigator
) {
    val backStack: List<Route> by navigator.backStack.collectAsState()
    val previousBackStackSize: Int by remember { mutableStateOf(backStack.size) }
    val isForwardNavigation: Boolean by derivedStateOf { backStack.size > previousBackStackSize }
    val route: Route = backStack.lastOrNull() ?: return
    AnimatedContent(
        targetState = route,
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
        val screen by produceUiState {
            val node = navigator.loadRouteNode(it)
            SDCNavigationScreen(screenNode = node, states = navigator.states)
        }
        SDCLoaderLayout(state = screen) {
            NavigationDestination {
                it.Content()
            }
        }
    }
}