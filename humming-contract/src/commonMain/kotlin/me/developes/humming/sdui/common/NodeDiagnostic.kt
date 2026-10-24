package me.developes.humming.sdui.common

enum class DiagnosticSeverity { ERROR, WARNING }

data class NodeDiagnostic(
    val code: String,
    val message: String,
    val operation: String,
    val path: String = "$",
    val nodeId: String? = null,
    val documentId: String? = null,
    val severity: DiagnosticSeverity = DiagnosticSeverity.ERROR,
)

class PayloadException(
    val diagnostic: NodeDiagnostic,
    cause: Throwable? = null,
) : IllegalArgumentException("${diagnostic.code} at ${diagnostic.path}: ${diagnostic.message}", cause)

data class PayloadLimits(
    val maxInputChars: Int = 2_000_000,
    val maxDepth: Int = 64,
    val maxNodes: Int = 10_000,
    val maxJsonValues: Int = 100_000,
    val maxPropertyChars: Int = 65_536,
) {
    init {
        require(maxInputChars > 0 && maxDepth > 0 && maxNodes > 0 &&
            maxJsonValues > 0 && maxPropertyChars > 0) { "Payload limits must be positive" }
    }
}

internal data class NodeOrigin(
    val path: String = "$",
    val documentId: String? = null,
    val limits: PayloadLimits = PayloadLimits(),
    val nodeId: String? = null,
)

internal val ServerDrivenNode.origin: NodeOrigin
    get() = (this as? JsonServerDrivenNode)?.sourceOrigin ?: NodeOrigin()

val ServerDrivenNode.sourcePath: String get() = origin.path

fun ServerDrivenNode.qualifiedName(): Pair<String, String> = qualifiedName(component, origin, id)

fun ServerDrivenNode.diagnostic(
    code: String, message: String, operation: String, path: String = origin.path,
) = NodeDiagnostic(code, message, operation, path, id, origin.documentId)

internal fun qualifiedName(name: String, origin: NodeOrigin, nodeId: String?): Pair<String, String> {
    val parts = name.split(':')
    if (parts.size != 2 || parts.any { it.isBlank() || it.any(Char::isWhitespace) }) {
        throw PayloadException(NodeDiagnostic(
            "INVALID_IDENTIFIER", "Expected namespace:name with two non-empty segments",
            "resolve", "${origin.path}.component", nodeId, origin.documentId,
        ))
    }
    return parts[0] to parts[1]
}
