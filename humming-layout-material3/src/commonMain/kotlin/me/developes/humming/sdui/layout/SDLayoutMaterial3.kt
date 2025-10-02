package me.developes.humming.sdui.layout

import me.developes.humming.sdui.common.ComponentHandler
import me.developes.humming.sdui.common.SDLibrary
import me.developes.humming.sdui.layout.components.SDCAnimatedVisibility
import me.developes.humming.sdui.layout.components.SDCBox
import me.developes.humming.sdui.layout.components.SDCButton
import me.developes.humming.sdui.layout.components.SDCButtonIcon
import me.developes.humming.sdui.layout.components.SDCButtonText
import me.developes.humming.sdui.layout.components.SDCColumn
import me.developes.humming.sdui.layout.components.SDCDivider
import me.developes.humming.sdui.layout.components.SDCImage
import me.developes.humming.sdui.layout.components.SDCOutlinedButtonText
import me.developes.humming.sdui.layout.components.SDCRow
import me.developes.humming.sdui.layout.components.SDCScaffold
import me.developes.humming.sdui.layout.components.SDCStyle
import me.developes.humming.sdui.layout.components.SDCText
import me.developes.humming.sdui.layout.components.SDCTextField
import me.developes.humming.sdui.layout.components.SDCTopAppBar

class SDLayoutMaterial3 : SDLibrary("layout") {
    override fun getComponent(name: String): ComponentHandler? {
        return when (name) {
            "box" -> return { node, viewModel -> SDCBox(node, viewModel) }
            "column" -> return { node, viewModel -> SDCColumn(node, viewModel) }
            "row" -> return { node, viewModel -> SDCRow(node, viewModel) }
            "text" -> return { node, viewModel -> SDCText(node, viewModel) }
            "textField" -> return { node, viewModel -> SDCTextField(node, viewModel) }
            "animatedVisibility" -> return { node, viewModel -> SDCAnimatedVisibility(node, viewModel) }
            "imageFile" -> return { node, viewModel -> SDCImage(node, viewModel) }
            "button" -> return { node, viewModel -> SDCButton(node, viewModel) }
            "outlinedButton" -> return { node, viewModel -> SDCOutlinedButtonText(node, viewModel) }
            "textButton" -> return { node, viewModel -> SDCButtonText(node, viewModel) }
            "iconButton" -> return { node, viewModel -> SDCButtonIcon(node, viewModel) }
            "scaffold" -> return { node, viewModel -> SDCScaffold(node, viewModel) }
            "topAppBar" -> return { node, viewModel -> SDCTopAppBar(node, viewModel) }
            "divider" -> return { node, viewModel -> SDCDivider(node, viewModel) }
            "style" -> return { node, viewModel -> SDCStyle(node, viewModel) }
            else -> null
        }
    }
}