package br.com.developes.sdui.events.lifecycle

/**
 * A lifecycle tracker that can be used to track the lifecycle of a component.
 */
interface LifecycleObserver {
    fun onEvent(event: LifecycleEvent) {
        // handle event
    }
}
