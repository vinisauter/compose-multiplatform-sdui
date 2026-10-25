package me.developes.humming.sdui.navigation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import me.developes.humming.sdui.common.ErrorData
import me.developes.humming.sdui.navigation.JourneyDefinitions
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.ScreenStateMode
import me.developes.humming.sdui.common.LocalScreenFeedbackHandler
import me.developes.humming.sdui.common.PayloadException
import me.developes.humming.sdui.common.diagnostic
import me.developes.humming.sdui.navigation.NavigationDefinitions

class Graph(
    private val graphNode: ServerDrivenNode,
    private val viewModel: SDUIFormViewModel,
    private val preLoadContent: (@Composable () -> Unit)? = null
) : ComposableContent {
    constructor(
        graphNode: ServerDrivenNode,
        viewModel: SDUIFormViewModel,
        definitions: JourneyDefinitions,
        preLoadContent: (@Composable () -> Unit)? = null,
    ) : this(graphNode, viewModel, preLoadContent) {
        useDefinitions(definitions)
    }

    private var navController: NavHostController? = null
    private val graphProperties = NavigationDefinitions.graph.parse(graphNode).requireValid()
    private val startRouteDestination = graphProperties.property("startDestination")!!
    private val graphRoutes: HashMap<String, ServerDrivenNode> =
        HashMap<String, ServerDrivenNode>().also { map ->
            graphNode.children?.let {
                for (serverDrivenNode in it) {
                    if (serverDrivenNode.component == "navigation:node") {
                        val properties = NavigationDefinitions.node.parse(serverDrivenNode).requireValid()
                        val name = properties.property("name")!!
                        map[name] = serverDrivenNode
                    }
                }
            }
        }
    private val componentNodes = mutableStateMapOf<String, ServerDrivenNode>()
    private var definitions: JourneyDefinitions? = null

    internal fun useDefinitions(definitions: JourneyDefinitions?) {
        check(navController == null && componentNodes.isEmpty()) { "Journey identity must be set before mounting Graph" }
        this.definitions = definitions
    }

    private suspend fun loadComponentNode(routeName: String, owner: SDUIFormViewModel = viewModel, reload: Boolean = false): ServerDrivenNode {
        if (definitions == null && !reload) componentNodes[routeName]?.let { return it }
        return let {
            val graphRouteNode = graphRoutes[routeName] ?: throw PayloadException(
                graphNode.diagnostic("UNKNOWN_ROUTE", "Navigation route is not registered: $routeName", "navigate"))
            val nodeType = graphRouteNode.property("type")!!
            val destiny = graphRouteNode.property("destiny")!!
            val componentNode = definitions?.load(routeName, nodeType, destiny) ?: run {
                val provider = owner.loadNodeTypeProvider(nodeType) ?: throw PayloadException(
                    graphRouteNode.diagnostic("MISSING_PROVIDER", "Provider is not registered: $nodeType", "navigate"))
                provider(destiny)
            }
            currentCoroutineContext().ensureActive()
            componentNodes[routeName] = componentNode
            componentNode
        }
    }

    @Composable
    override fun Content() {
        val navController = rememberNavController()
        this.navController = navController
        DisposableEffect(navController) { onDispose { if (this@Graph.navController === navController) this@Graph.navController = null } }
        NavHost(navController = navController, startDestination = "PreLoadContent") {
            composable("PreLoadContent") {
                preLoadContent?.invoke()
                LaunchedEffect(Unit) {
                    val task = viewModel.runTask(block = {
                        loadComponentNode(startRouteDestination)
                        navController.navigate(startRouteDestination) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }, onError = { loadError(startRouteDestination, it) })
                    try { task.join() } finally { task.cancel() }
                }
            }
            graphRoutes.forEach { (routeName, routeNode) ->
                composable(
                    routeName,
                    deepLinks = routeNode.propertyJsonArray("deepLinks")
                        ?.mapNotNull { it.jsonPrimitive.contentOrNull }?.map {
                            navDeepLink { uriPattern = it }
                        } ?: emptyList(),
                ) { entry ->
                    val owner = if (viewModel.screenStateMode == ScreenStateMode.ISOLATED_ENTRIES) {
                        viewModel<SDUIFormViewModel>(viewModelStoreOwner = entry, key = "humming-screen") {
                            viewModel.createScreenEntry(entry.id, viewModel.entryInputs(entry.id,routeName))
                        }
                    } else viewModel
                    if (owner !== viewModel) LocalScreenFeedbackHandler.current(owner)
                    val node = componentNodes[routeName]
                    if (node == null) {
                        preLoadContent?.invoke()
                        LaunchedEffect(owner, routeName) {
                            val task = owner.runTask(block = { loadComponentNode(routeName, owner) },
                                onError = { loadError(routeName, it) })
                            try { task.join() } finally { task.cancel() }
                        }
                    } else {
                        val content = remember(owner, node) { owner.loadComponent(node) }
                        content?.Content()
                    }
                }
            }
        }
    }

    private fun loadError(route: String, error: Throwable): ErrorData =
        if (error is PayloadException) ErrorData.payload(error) else {
            val node = graphRoutes[route] ?: graphNode
            ErrorData.providerError(node.property("type") ?: "navigation").copy(
                diagnostic = node.diagnostic("PROVIDER_ERROR", "Route definition could not be loaded: $route", "loadRoute"),
                cause = error,
            )
        }

    /** Host-triggered reload. Keeps the currently displayed definition until a validated replacement is ready. */
    suspend fun reloadRoute(routeName: String, owner: SDUIFormViewModel = viewModel) {
        val route = graphRoutes[routeName] ?: throw PayloadException(
            graphNode.diagnostic("UNKNOWN_ROUTE", "Navigation route is not registered: $routeName", "reloadRoute"))
        definitions?.invalidate(routeName, route.property("type")!!, route.property("destiny")!!)
        loadComponentNode(routeName, owner, reload = true)
    }

    fun navigateBack() {
        navController?.popBackStack()
    }

    /** Host entry point; call on the UI thread. Only deep links registered in this graph can match. */
    fun openDeepLink(uri: String): Boolean {
        require(uri.isNotBlank() && uri.length <= 4096) { "Deep link must contain 1..4096 characters" }
        return navController?.handleDeepLink(NavDeepLinkRequest.Builder.fromUri(NavUri(uri)).build()) ?: false
    }

    fun navigateTo(
        routeName: String,
        popStack: Boolean = false,
        clearStack: Boolean = false
    ) {
        viewModel.runTask(block = { navigateToAwaited(routeName, popStack, clearStack) })
    }

    suspend fun navigateToAwaited(
        routeName: String,
        popStack: Boolean = false,
        clearStack: Boolean = false,
        owner: SDUIFormViewModel = viewModel,
    ) {
            loadComponentNode(routeName, owner)// Preload the component node
            if (clearStack) {
                navController?.navigate(routeName) {
                    popUpTo(0) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            } else if (popStack) {
                navController?.navigate(routeName) {
                    popUpTo(routeName) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            } else {
                navController?.navigate(routeName)
            }
    }
}
