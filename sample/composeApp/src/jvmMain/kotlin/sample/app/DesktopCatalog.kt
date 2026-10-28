package sample.app

import me.developes.humming.builder.ui.*

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.nio.file.Files
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

/** Desktop file chooser is opened only by an explicit user gesture. */
class DesktopStudioFiles : StudioFiles {
    override fun open(result: (StudioFileResult) -> Unit) { SwingUtilities.invokeLater {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                val file = chooser.selectedFile.toPath()
                if (Files.size(file) > 32_000_000) result(StudioFileResult(error = "Arquivo excede o limite"))
                else result(StudioFileResult(content = Files.readString(file)))
            } catch (_: java.io.IOException) { result(StudioFileResult(error = "Não foi possível ler o arquivo")) }
        }
    } }
    override fun download(name: String, content: String) { SwingUtilities.invokeLater {
        val chooser = JFileChooser().apply { selectedFile = java.io.File(name) }
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val target = chooser.selectedFile.toPath()
            if (Files.exists(target) && javax.swing.JOptionPane.showConfirmDialog(null, "Substituir o arquivo existente?", "Salvar", javax.swing.JOptionPane.YES_NO_OPTION) != javax.swing.JOptionPane.YES_OPTION) return@invokeLater
            try { Files.writeString(target, content) }
            catch (_: java.io.IOException) { javax.swing.JOptionPane.showMessageDialog(null, "Não foi possível salvar o arquivo") }
        }
    } }
}

@Composable fun DesktopCatalog() {
    val client = remember { HttpClient(OkHttp) { followRedirects = false; expectSuccess = false } }
    DisposableEffect(client) { onDispose { client.close() } }
    val service = remember {
        val origin = System.getenv("HUMMING_STUDIO_ORIGIN")
        val tenant = System.getenv("HUMMING_STUDIO_TENANT")
        val document = System.getenv("HUMMING_STUDIO_DOCUMENT")
        val token = System.getenv("HUMMING_STUDIO_TOKEN")
        if (listOf(origin, tenant, document, token).any { it.isNullOrBlank() }) null
        else if (!Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}").matches(tenant!!)) null
        else try { CatalogServices(client, document!!, "${origin!!.trimEnd('/')}/v1/tenants/$tenant/documents/$document", token!!) }
        catch (_: IllegalArgumentException) { null }
    }
    OfficialCatalogApp(files = remember { DesktopStudioFiles() }, remoteContent = { DesktopRemoteStudio() },
        servicesContent = {
            if (service != null) CatalogServicesPanel(service)
            else Text("Configure HUMMING_STUDIO_ORIGIN/TENANT/DOCUMENT/TOKEN conforme sample/README.md para usar serviços reais.")
        })
}
