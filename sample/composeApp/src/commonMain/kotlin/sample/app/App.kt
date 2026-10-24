package sample.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import br.com.developes.sdui.resources.Res
import br.com.developes.sdui.resources.ic_eye
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.delay
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ShimmerEffect
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.getPlatform
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.navigation.SDNavigation
import org.jetbrains.compose.resources.painterResource

@Composable
fun App(
    initialResource: String = "files/navigation/app-navigation.json",
    savedStateHandleFactory: (() -> SavedStateHandle)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        var viewModel: SDUIFormViewModel? = null
        HummingSDUI().Content(
            HummingSDUI.Config(
                type = "file",
                destiny = initialResource,
                theme = Material3Theme(),
                placeholder = {
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    )
                },
                loadingHandler = {
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    )
                },
                errorHandler = { error, viewModel ->
                    AlertDialog(
                        modifier = Modifier,
                        onDismissRequest = { viewModel.dismissError() },
                        title = { Text(text = "Humming ERROR") },
                        text = { Text(text = error.message
                        ) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text("OK")
                            }
                        }
                    )
                },
            ) {
                // You can add custom components here
                // Example of a custom node provider that fetches JSON from a URL
                addNodeProvider("url") { res ->
                    delay(2000)
                    val client = HttpClient()
                    val response = client.get(res)
                    val json = response.bodyAsText()
                    return@addNodeProvider parseNode(json, documentId = res)
                }
                addNodeProvider("file") { res ->
                    delay(1000)
                    val bytes = Res.readBytes(res)
                    val json = bytes.decodeToString()
                    return@addNodeProvider parseNode(json, documentId = res)
                }
                // You can add custom libraries here
                addLibrary(SDActions().apply {
                    registerMethod("getAppPlatform") { node, vm ->
                        val state = node.property("state") ?: "platformName"
                        vm.updateState(state, getPlatform().name)
                    }
                })
                addLibrary(SDLayoutMaterial3())
                addLibrary(SDNavigation())
//                addLibrary(SDLibrary("layout") {})
//                addLibrary(SDLibrary("container") {})
//                addLibrary(SDLibrary("widget") {})
                viewModel = this
            },
            savedStateHandleFactory = savedStateHandleFactory,
        )
        if (viewModel == null) return
        Column(
            modifier = Modifier.background(color = Color.Yellow)
                .align(Alignment.TopEnd)
        ) {
            var statesVisibility by remember { mutableStateOf(true) }
            Image(
                painter = painterResource(Res.drawable.ic_eye),
                contentDescription = "Settings",
                modifier = Modifier.size(24.dp).clickable {
                    statesVisibility = !statesVisibility
                }
            )
            if (statesVisibility) {
                val states = viewModel!!.formState
                for (entry in states) {
                    Text(
                        modifier = Modifier,
                        text = "key: ${entry.key}, value: ${entry.value}"
                    )
                }
            }
        }
    }
}
