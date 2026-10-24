package me.developes.humming.sdui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** One root action execution owns these slots; nested sequences inherit them. */
class ActionValues internal constructor() {
    private val responseValue = MutableStateFlow<SduiValue>(SduiValue.Missing)
    private val errorValue = MutableStateFlow<SduiValue>(SduiValue.Missing)
    var response: SduiValue
        get() = responseValue.value
        set(value) { responseValue.value = value }
    var error: SduiValue
        get() = errorValue.value
        set(value) { errorValue.value = value }
    internal fun clear() { responseValue.value=SduiValue.Missing; errorValue.value=SduiValue.Missing }
}

internal class ActionValuesContext(val owner: SDUIFormViewModel, val values: ActionValues) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<ActionValuesContext>
}

/** The typed map is the authority; this is only the live textual compatibility view. */
internal class LegacyStateAccess(private val values: MutableMap<String,SduiValue>) {
    operator fun get(key: String): String? = values[key]?.toLegacyText()
    operator fun set(key: String, value: String) { values[key]=SduiValue.Text(value) }
    fun remove(key: String) { values.remove(key) }
    val view: Map<String,String> = object : AbstractMap<String,String>() {
        override val entries: Set<Map.Entry<String,String>> get() =
            this@LegacyStateAccess.values.mapValues { checkNotNull(it.value.toLegacyText()) }.entries
        override fun get(key: String) = this@LegacyStateAccess[key]
        override fun containsKey(key: String) = this@LegacyStateAccess.values.containsKey(key)
        override val size get() = this@LegacyStateAccess.values.size
    }
}
