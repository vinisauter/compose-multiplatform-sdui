package me.developes.humming.builder

import me.developes.humming.builder.ui.StudioFileResult
import me.developes.humming.builder.ui.StudioFiles
import java.nio.file.Files
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class BuilderDesktopFiles : StudioFiles {
    override fun open(result: (StudioFileResult) -> Unit) = SwingUtilities.invokeLater {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                val path = chooser.selectedFile.toPath()
                if (Files.size(path) > 32_000_000) result(StudioFileResult(error = "Arquivo excede 32 MB"))
                else result(StudioFileResult(content = Files.readString(path)))
            } catch (_: java.io.IOException) { result(StudioFileResult(error = "Não foi possível abrir o projeto")) }
        }
    }

    override fun download(name: String, content: String) = SwingUtilities.invokeLater {
        val chooser = JFileChooser().apply { selectedFile = java.io.File(name) }
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val path = chooser.selectedFile.toPath()
            if (Files.exists(path) && JOptionPane.showConfirmDialog(null, "Substituir o arquivo existente?",
                    "Salvar projeto", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return@invokeLater
            try { Files.writeString(path, content) }
            catch (_: java.io.IOException) { JOptionPane.showMessageDialog(null, "Não foi possível salvar o projeto") }
        }
    }
}
