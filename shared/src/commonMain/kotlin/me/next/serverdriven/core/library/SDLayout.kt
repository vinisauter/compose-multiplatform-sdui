package me.next.serverdriven.core.library

import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.components.SDCAnimatedVisibility
import me.next.serverdriven.compose.components.SDCButton
import me.next.serverdriven.compose.components.SDCButtonText
import me.next.serverdriven.compose.components.SDCColumn
import me.next.serverdriven.compose.components.SDCRow
import me.next.serverdriven.compose.components.SDCText
import me.next.serverdriven.compose.components.SDCTextField
import me.next.serverdriven.compose.components.fromNode
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
        addComponent("button") { node, state -> SDCButton(node, state) }
        addComponent("textButton") { node, state -> SDCButtonText(node, state) }
    }

    @OptIn(ExperimentalResourceApi::class)
    fun imageFileLoader() =
        SDComponentLoader(load = { node, _ ->
            val modifier = Modifier.fromNode(node)
            val contentDescription: String? = node.property("contentDescription")
            val resource = node.property("painterResource")!!

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