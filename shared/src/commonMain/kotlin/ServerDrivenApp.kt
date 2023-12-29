import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import me.next.serverdriven.apollo.ApolloProvider
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.compose.SDCLibrary.Companion.loadNodeTypeProvider
import me.next.serverdriven.compose.SDCLibrary.Companion.registerNodeTypeProvider
import me.next.serverdriven.compose.SDCLoaderLayout
import me.next.serverdriven.compose.produceUiState

@Composable
fun ServerDrivenApp() {
    val scope = rememberCoroutineScope()
    val apolloClient = ApolloProvider()
    SDCLibrary(debug = true) {
        val stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
        //https://docs.parseplatform.org/graphql/guide/#users
        LaunchedEffect(Unit) {
            stateMap["health"] = apolloClient.health().toString()
        }
        registerNodeTypeProvider("graphQL") { nodeId ->
            apolloClient.getNode(nodeId)!!
        }
        val uiState by produceUiState {
            loadNodeTypeProvider("graphQL").invoke("app-navigation")
        }
        SDCLoaderLayout(
            modifier = Modifier,
            state = uiState,
        ) { node ->
            SDCLibrary.loadComponent(node, stateMap)
        }
    }

//    LaunchedEffect(Unit) {
//        apolloClient.init()
//        apolloClient.createNode("navigation/app-navigation.json")
//        apolloClient.createNode("layout/login.json")
//        apolloClient.createNode("layout/menu.json")
//        apolloClient.createNode("layout/static.json")
//        apolloClient.createNode("layout/template.json")
//    }
}

@Composable
fun ServerDrivenAppLocal() {
    SDCLibrary(debug = true) {
        val stateMap: MutableMap<String, String> = remember { mutableStateMapOf() }
        val graph = "navigation/app-navigation.json"
        val uiState by produceUiState {
            loadNodeTypeProvider("file").invoke(graph)
        }
        SDCLoaderLayout(modifier = Modifier, state = uiState) { node ->
            SDCLibrary.loadComponent(node, stateMap)
        }
    }
}