package br.com.developes.sdui.events

import androidx.compose.runtime.DisposableEffect
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.SDCLibrary.Companion.launchHandling
import br.com.developes.sdui.SDLibrary
import br.com.developes.sdui.events.lifecycle.LifecycleEvent
import br.com.developes.sdui.events.lifecycle.LifecycleObserver
import br.com.developes.sdui.events.lifecycle.LocalLifecycleTracker
import br.com.developes.sdui.utils.LocalCoroutineScope
import br.com.developes.sdui.utils.serverDrivenNode
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SDEvent : SDLibrary("event") {
    init {
        addComponent("loader") { node, states ->
            //TODO: check lifecycle of @Composable br.com.developes.sdui.navigation.Screen
            // current is in Activity/ViewController && @Composable<init>
            val onCreate = node.propertyNodes("onCreate").let { SDCLibrary.loadActions(it) }
            val onStart = node.propertyNodes("onStart").let { SDCLibrary.loadActions(it) }
            val onResume = node.propertyNodes("onResume").let { SDCLibrary.loadActions(it) }
            val onPause = node.propertyNodes("onPause").let { SDCLibrary.loadActions(it) }
            val onStop = node.propertyNodes("onStop").let { SDCLibrary.loadActions(it) }
            val onDestroy = node.propertyNodes("onDestroy").let { SDCLibrary.loadActions(it) }

            val ifState = node.propertyJsonArray("ifState")

            val coroutineScope = LocalCoroutineScope.current
            val lifecycleTracker = LocalLifecycleTracker.current
            DisposableEffect(Unit) {
                val listener = object : LifecycleObserver {
                    override fun onEvent(event: LifecycleEvent) {
                        when (event) {
                            LifecycleEvent.OnCreateEvent -> coroutineScope.launchHandling {
                                onCreate.invoke(node, states)
                            }

                            LifecycleEvent.OnStartEvent -> coroutineScope.launchHandling {
                                onStart.invoke(node, states)
                            }

                            LifecycleEvent.OnResumeEvent -> coroutineScope.launchHandling {
                                onResume.invoke(node, states)
                            }

                            LifecycleEvent.OnPauseEvent -> coroutineScope.launchHandling {
                                onPause.invoke(node, states)
                            }

                            LifecycleEvent.OnStopEvent -> coroutineScope.launchHandling {
                                onStop.invoke(node, states)
                            }

                            LifecycleEvent.OnDestroyEvent -> coroutineScope.launchHandling {
                                onDestroy.invoke(node, states)
                            }
                        }
                    }
                }
                lifecycleTracker.addObserver(listener)
                onDispose {
                    lifecycleTracker.removeObserver(listener)
                }
            }

            ifState?.forEach { jsonElement ->
                jsonElement.jsonObject.let { ifS ->
                    val stateName = ifS["state"]?.jsonPrimitive?.content
                    val thenActions =
                        ifS["then"]?.serverDrivenNode?.let { SDCLibrary.loadActions(it) }
                    val elseActions =
                        ifS["else"]?.serverDrivenNode?.let { SDCLibrary.loadActions(it) }
                    if (stateName != null) {
                        states[stateName]?.let { value ->
                            if (value.toBoolean()) coroutineScope.launchHandling {
                                thenActions?.invoke(node, states)
                            } else coroutineScope.launchHandling {
                                elseActions?.invoke(node, states)
                            }
                        }
                    }
                }
            }
        }
    }
}