package me.developes.humming.sdui.common

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

data class NativeRequest(val capability: String, val input: SduiValue = SduiValue.ObjectValue(emptyMap()), val version: Int = 1)

sealed interface NativeResult {
    data class Success(val value: SduiValue) : NativeResult
    data class Rejected(val code: String) : NativeResult {
        init { require(Regex("[A-Z][A-Z0-9_]{0,63}").matches(code)) }
    }
    data object Unavailable : NativeResult
}

typealias NativeCapabilityHandler = suspend (SduiValue) -> NativeResult

/** Explicit host registry. No reflection, platform discovery, permissions or payload-selected code. */
class NativeBridge(handlers: Map<String, NativeCapabilityHandler>) {
    private val handlers = handlers.toMap()
    val capabilities: Set<String> get() = handlers.keys.toSet()

    init {
        require(handlers.size <= 256)
        require(handlers.keys.all { it.length <= 128 && Regex("[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)+").matches(it) })
    }

    suspend fun invoke(request: NativeRequest): NativeResult {
        currentCoroutineContext().ensureActive()
        if (request.version != 1) return NativeResult.Rejected("NATIVE_VERSION")
        val handler = handlers[request.capability] ?: return NativeResult.Unavailable
        if (!withinBudget(request.input)) return NativeResult.Rejected("NATIVE_INPUT_LIMIT")
        val result = handler(request.input)
        currentCoroutineContext().ensureActive()
        check(result !is NativeResult.Success || withinBudget(result.value)) { "Native handler returned an invalid or excessive value" }
        return result
    }

    private fun withinBudget(value: SduiValue): Boolean {
        val pending = ArrayDeque<Pair<SduiValue, Int>>()
        pending.add(value to 1)
        var nodes = 0
        var chars = 0L
        while (pending.isNotEmpty()) {
            val (item, depth) = pending.removeLast()
            if (++nodes > 10_000 || depth > 64) return false
            when (item) {
                SduiValue.Missing -> return false
                SduiValue.Null, is SduiValue.BooleanValue -> chars += 5
                is SduiValue.NumberValue -> chars += item.source.length
                is SduiValue.Text -> { if (item.value.length > 65_536) return false; chars += item.value.length }
                is SduiValue.ArrayValue -> {
                    if (item.values.size + nodes + pending.size > 10_000) return false
                    item.values.forEach { pending.add(it to depth + 1) }
                }
                is SduiValue.ObjectValue -> {
                    if (item.values.size + nodes + pending.size > 10_000) return false
                    item.values.forEach { (key, child) ->
                        if (key.length > 256) return false
                        chars += key.length
                        pending.add(child to depth + 1)
                    }
                }
            }
            if (chars > 1_000_000) return false
        }
        return true
    }
}
