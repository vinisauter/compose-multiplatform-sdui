package sample.app

import me.developes.humming.builder.ui.*

import androidx.compose.material3.*
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.studio.RemoteDocument
import java.nio.file.Path

private class DesktopRemoteSetup(val identity: RemoteDocument, val api: JvmRemoteStudioApi, val recovery: JvmRemoteStudioRecovery)
private fun configuredRemote(): DesktopRemoteSetup? {
    val names = listOf("HUMMING_STUDIO_ORIGIN", "HUMMING_STUDIO_CONNECTION", "HUMMING_STUDIO_TENANT",
        "HUMMING_STUDIO_DOCUMENT", "HUMMING_STUDIO_TOKEN", "HUMMING_STUDIO_CHECKPOINT")
    val values = names.map(System::getenv)
    if (values.any { it.isNullOrBlank() }) return null
    return try {
        val identity = RemoteDocument(values[1]!!, values[2]!!, values[3]!!)
        val recovery = JvmRemoteStudioRecovery(Path.of(values[5]!!))
        val api = JvmRemoteStudioApi(values[0]!!, identity, values[4]!!)
        DesktopRemoteSetup(identity, api, recovery)
    } catch (_: IllegalArgumentException) { null }
}

/** Selected only by --remote-studio. The normal sample/browser remains local. */
@Composable
fun DesktopRemoteStudio() {
    val setup = remember { configuredRemote() }
    var attempt by remember { mutableStateOf(0) }
    var remote by remember { mutableStateOf<RemoteStudioHost?>(null) }
    var message by remember { mutableStateOf("Carregando recuperação/rascunho remoto…") }
    DisposableEffect(setup) { onDispose { setup?.api?.close() } }
    LaunchedEffect(setup, attempt) {
        if (setup == null) message = "Configuração remota incompleta ou inválida; consulte docs/studio.md"
        else {
            val result = RemoteStudioHost.open(setup.identity, initialNodeCatalog(), setup.api, setup.recovery)
            remote = result.value
            if (remote == null) message = result.diagnostics.joinToString { it.code }
        }
    }
    val connected = remote
    if (connected != null) LocalStudio(workspace = connected.workspace, remoteHost = connected)
    else MaterialTheme { Column {
        Text(message)
        Button(enabled = setup != null, onClick = { attempt++ }) { Text("Tentar conectar novamente") }
    } }
}
