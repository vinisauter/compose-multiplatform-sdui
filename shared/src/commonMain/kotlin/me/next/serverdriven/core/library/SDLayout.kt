package me.next.serverdriven.core.library

import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import me.next.serverdriven.compose.components.SDCColumn
import me.next.serverdriven.compose.components.SDCRow
import me.next.serverdriven.compose.components.SDCText
import me.next.serverdriven.compose.components.fromNode
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.resource


class SDLayout : SDLibrary("layout") {
    init {
        addComponent("column") { SDCColumn(it) }
        addComponent("row") { SDCRow(it) }
        addComponent("text") { SDCText(it) }
        addComponent("imageFile", ImageFileLoader())

// TODO: INTERACTION
//  // TextField
//        addComponent("textField"){
//            val textState = remember { mutableStateOf(TextFieldValue()) }
//            TextField(
//                value = textState.value,
//                onValueChange = { textState.value = it }
//            )
//            Text("The textfield has this text: " + textState.value.text)
//        }
//  // Button
//        addComponent("textButton"){
//            var greetingText by remember { mutableStateOf("Hello, World!") }
//            var showImage by remember { mutableStateOf(false) }
//            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//                Button(onClick = {
//                    greetingText = "Hello, Click"
//                    showImage = !showImage
//                }) {
//                    Text(greetingText)
//                }
//            }
//        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private class ImageFileLoader :
        SDComponentLoader<Triple<Modifier, String, String?>>(load = { node ->
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