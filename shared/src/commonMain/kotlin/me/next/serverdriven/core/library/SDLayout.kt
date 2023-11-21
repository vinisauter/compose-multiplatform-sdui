package me.next.serverdriven.core.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import getPlatformName
import me.next.serverdriven.compose.components.SDCAnimatedVisibility
import me.next.serverdriven.compose.components.SDCColumn
import me.next.serverdriven.compose.components.SDCRow
import me.next.serverdriven.compose.components.SDCText
import me.next.serverdriven.compose.components.SDCTextField
import me.next.serverdriven.compose.components.fromNode
import me.next.serverdriven.compose.loadComponent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.resource

class SDLayout : SDLibrary("layout") {
    init {
        addComponent("column") { node, state -> SDCColumn(node, state) }
        addComponent("row") { node, state -> SDCRow(node, state) }
        addComponent("text") { node, state -> SDCText(node, state) }
        addComponent("textField") { node, state -> SDCTextField(node, state) }
        addComponent("animatedVisibility") { node, state -> SDCAnimatedVisibility(node, state) }
        addComponent("imageFile", imageFileLoader())

        //  TODO: Button
        addComponent("button") { node, state ->
            Button(onClick = {
                state["showImage"] = (!(state["showImage"]?.toBoolean() ?: true)).toString()
                state["greetingText"] = getPlatformName()
                state["platformName"] = getPlatformName()
            }) {
                node.children?.let {
                    for (serverDrivenNode in it) {
                        loadComponent(node = serverDrivenNode, dataState = state)
                    }
                }
            }
        }
        addComponent("textButton") { node, state ->
            var greetingText by remember { mutableStateOf("Hello, World!") }
            var showImage by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    greetingText = "Hello, Click"
                    showImage = !showImage

                    state["greetingText"] = getPlatformName()
                    state["platformName"] = getPlatformName()
                    state["showImage"] =
                        (!(state["showImage"]?.toBoolean() ?: false)).toString()
                }) {
                    Text(greetingText)
                }
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    fun imageFileLoader() =
        SDComponentLoader(load = { node, _ ->
            val modifier = Modifier.fromNode(node.properties)
            val contentDescription: String? = node.properties?.get("contentDescription")?.toString()
            val resource = node.properties?.get("painterResource")?.toString()!!

            // FIXME: ExperimentalResourceApi
            //  LANÇAR EXCEPTION ANTES DO COMPOSABLE (painterResource)
            resource(resource).readBytes()

            Triple(modifier, resource, contentDescription)
        }, into = {
            val (modifier, resource, contentDescription) = it
            Image(
                modifier = modifier,
                painter = painterResource(resource),
                contentDescription = contentDescription
            )
        })
}