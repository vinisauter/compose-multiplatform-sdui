package me.next.serverdriven.core.library

import me.next.serverdriven.compose.components.SDCAnimatedVisibility
import me.next.serverdriven.compose.components.SDCButton
import me.next.serverdriven.compose.components.SDCButtonText
import me.next.serverdriven.compose.components.SDCColumn
import me.next.serverdriven.compose.components.SDCImage
import me.next.serverdriven.compose.components.SDCRow
import me.next.serverdriven.compose.components.SDCText
import me.next.serverdriven.compose.components.SDCTextField

class SDLayout : SDLibrary("layout") {
    init {
        addComponentLayout("column") { node, state -> SDCColumn(node, state) }
        addComponentLayout("row") { node, state -> SDCRow(node, state) }
        addComponentLayout("text") { node, state -> SDCText(node, state) }
        addComponentLayout("textField") { node, state -> SDCTextField(node, state) }
        addComponentLayout("animatedVisibility") { node, state -> SDCAnimatedVisibility(node, state) }
        addComponentLayout("imageFile") { node, state -> SDCImage(node, state) }
        addComponentLayout("button") { node, state -> SDCButton(node, state) }
        addComponentLayout("textButton") { node, state -> SDCButtonText(node, state) }
    }
}