package me.developes.humming.builder.ui

data class StudioFileResult(val content: String? = null, val error: String? = null)

/** Platform file access is supplied by the host, never by a payload or the authoring model. */
interface StudioFiles {
    fun open(result: (StudioFileResult) -> Unit)
    fun download(name: String, content: String)
}
