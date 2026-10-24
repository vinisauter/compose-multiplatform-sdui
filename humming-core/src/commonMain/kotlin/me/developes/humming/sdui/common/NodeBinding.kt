package me.developes.humming.sdui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** A cold binding: the consumer owns its subscription. */
class NodeBinding<T> internal constructor(
    private val evaluate: () -> T,
    changes: Flow<Int>,
) {
    val value: T get() = evaluate()
    val updates: Flow<T> = changes.map { evaluate() }.distinctUntilChanged()
}

@Composable
fun <T> NodeBinding<T>.collectAsState(): State<T> = updates.collectAsState(initial = value)
