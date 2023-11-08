package me.next.serverdriven.compose.components

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.next.serverdriven.core.tree.ServerDrivenNode

@Composable
fun SDCTextButton(node: ServerDrivenNode, action: () -> Unit): Unit {
    val text: String = node.properties?.get("text").toString()
    node.properties?.get("onClick")
//    enabled: Boolean = true,
//    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
//    elevation: ButtonElevation? = ButtonDefaults.elevation(),
//    shape: Shape = MaterialTheme.shapes.small,
//    border: BorderStroke? = null,
//    colors: ButtonColors = ButtonDefaults.buttonColors(),
//    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    Button(
        modifier = Modifier.fromNode(node.properties),
        onClick = action
    ) {
        Text(text = text)
    }
}