package me.developes.humming.sdui.navigation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

class Graph(
    private val graphNode: ServerDrivenNode,
    private val viewModel: SDUIFormViewModel,
    private val preLoadContent: (@Composable () -> Unit)? = null
) : ComposableContent {
    private var navController: NavHostController? = null
    private val startRouteDestination = graphNode.property("startDestination")!!
    private val graphRoutes: HashMap<String, ServerDrivenNode> =
        HashMap<String, ServerDrivenNode>().also { map ->
            graphNode.children?.let {
                for (serverDrivenNode in it) {
                    if (serverDrivenNode.component == "navigation:node") {
                        val name = serverDrivenNode.property("name")!!
                        serverDrivenNode.property("type")!!
                        serverDrivenNode.property("destiny")!!
                        map[name] = serverDrivenNode
                    }
                }
            }
        }
    private val componentNodes: HashMap<String, ServerDrivenNode> = HashMap()
    private suspend fun loadComponentNode(routeName: String): ServerDrivenNode {
        return componentNodes[routeName] ?: let {
            val graphRouteNode = graphRoutes[routeName]!!
            val nodeType = graphRouteNode.property("type")!!
            val destiny = graphRouteNode.property("destiny")!!
            val componentNode = viewModel.loadNodeTypeProvider(nodeType)!!.invoke(destiny)
            componentNodes[routeName] = componentNode
            componentNode
        }
    }

    @Composable
    override fun Content() {
        val navController = rememberNavController()
        this.navController = navController
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
                ) {
                    viewModel.loadComponent(node = componentNodes[routeName]!!)?.Content()
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
        viewModel.runTask({
            loadComponentNode(routeName)// Preload the component node
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
        })
    }
}