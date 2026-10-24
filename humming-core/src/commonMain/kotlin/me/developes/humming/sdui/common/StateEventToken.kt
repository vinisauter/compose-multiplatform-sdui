package me.developes.humming.sdui.common

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** Stable identity of one composed state-change listener. */
class StateEventToken

internal class StateMutationOrigin(val listeners: Set<StateEventToken>) :
    AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<StateMutationOrigin>
}
