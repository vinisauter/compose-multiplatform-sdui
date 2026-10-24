package me.developes.humming.sdui.navigation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
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
    private val componentNodes: HashMap<String, ServerDrivenNode> = HashMap()
    private suspend fun loadComponentNode(routeName: String, owner: SDUIFormViewModel = viewModel): ServerDrivenNode {
        return componentNodes[routeName] ?: let {
            val graphRouteNode = graphRoutes[routeName] ?: throw PayloadException(
                graphNode.diagnostic("UNKNOWN_ROUTE", "Navigation route is not registered: $routeName", "navigate"))
            val nodeType = graphRouteNode.property("type")!!
            val destiny = graphRouteNode.property("destiny")!!
            val provider = owner.loadNodeTypeProvider(nodeType) ?: throw PayloadException(
                graphRouteNode.diagnostic("MISSING_PROVIDER", "Provider is not registered: $nodeType", "navigate"))
            val componentNode = provider(destiny)
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
                    loadComponentNode(startRouteDestination)
                    navController.navigate(startRouteDestination) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
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
                    val node = componentNodes[routeName]!!
                    val content = remember(owner,node) { owner.loadComponent(node) }
                    if (owner !== viewModel) LocalScreenFeedbackHandler.current(owner)
                    content?.Content()
                }
            }
        }
    }

    fun navigateBack() {
        navController?.popBackStack()
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
