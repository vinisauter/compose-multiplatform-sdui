package me.next.serverdriven.core.library.layout

import me.next.serverdriven.core.library.SDLibrary
import me.next.serverdriven.core.library.layout.components.SDCAnimatedVisibility
import me.next.serverdriven.core.library.layout.components.SDCBox
import me.next.serverdriven.core.library.layout.components.SDCButton
import me.next.serverdriven.core.library.layout.components.SDCButtonText
import me.next.serverdriven.core.library.layout.components.SDCColumn
import me.next.serverdriven.core.library.layout.components.SDCImage
import me.next.serverdriven.core.library.layout.components.SDCRow
import me.next.serverdriven.core.library.layout.components.SDCText
import me.next.serverdriven.core.library.layout.components.SDCTextField

class SDLayout : SDLibrary("layout") {
    init {
        addComponentLayout("box") { node, state -> SDCBox(node, state) }
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