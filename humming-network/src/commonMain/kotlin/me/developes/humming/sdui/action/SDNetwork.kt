package me.developes.humming.sdui.action

import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.network.*

/** Only installs actions. The host owns and closes the transport's HttpClient. */
class SDNetwork(private val transport: SduiHttpTransport) : SDLibrary("network") {
    init {
        addDefinition(NetworkDefinitions.request)
        addAction("request") { node, owner ->
            NetworkDefinitions.request.parse(node).requireValid()
            val values = owner.actionValues()
            values.response = SduiValue.Missing
            values.error = SduiValue.Missing
            val literal = node.property("bodyJson")
            val key = node.property("bodyState")
            if (literal != null && key != null) throw PayloadException(node.diagnostic("NETWORK_BODY", "Supply either bodyJson or bodyState", "request"))
            val body = if (key != null) owner.stateValue(key) else literal?.let { source ->
                try { parseSduiValue(source) } catch (error: PayloadException) {
                    throw PayloadException(node.diagnostic(error.diagnostic.code, "Invalid JSON request body", "request",
                        "${node.sourcePath}.properties[\"bodyJson\"]${error.diagnostic.path.removePrefix("$")}"), error)
                }
            } ?: SduiValue.Missing
            val result = if (key != null && body === SduiValue.Missing) HttpOutcome.Failure("NETWORK_BODY_MISSING")
                else transport.request(checkNotNull(node.property("endpoint")), node.property("method") ?: "GET", body)
            val branch = when (result) {
                is HttpOutcome.Success -> {
                    values.response = SduiValue.ObjectValue(mapOf("status" to SduiValue.NumberValue(result.status.toString()), "body" to result.body))
                    "onSuccess"
                }
                is HttpOutcome.Failure -> {
                    values.error = SduiValue.ObjectValue(buildMap {
                        put("code", SduiValue.Text(result.code))
                        put("nodeId", SduiValue.Text(node.id))
                        put("path", SduiValue.Text(node.sourcePath))
                        put("operation", SduiValue.Text("request"))
                        result.status?.let { put("status", SduiValue.NumberValue(it.toString())) }
                    })
                    if (node.property("onFailure") == null) throw PayloadException(node.diagnostic(result.code,
                        "HTTP request failed${result.status?.let { " (status $it)" }.orEmpty()}", "request"))
                    "onFailure"
                }
            }
            owner.executeActions(node.propertyNodes(branch))
        }
    }
}
