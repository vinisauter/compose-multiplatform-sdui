package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.AngleDoubleRight
import compose.icons.fontawesomeicons.solid.CarCrash
import compose.icons.fontawesomeicons.solid.Clone
import compose.icons.fontawesomeicons.solid.CloudShowersHeavy
import compose.icons.fontawesomeicons.solid.Columns
import compose.icons.fontawesomeicons.solid.FistRaised
import compose.icons.fontawesomeicons.solid.LaptopMedical
import compose.icons.fontawesomeicons.solid.Passport
import compose.icons.fontawesomeicons.solid.Socks
import compose.icons.fontawesomeicons.solid.StreetView
import compose.icons.fontawesomeicons.solid.Underline
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.Layout

class SDCButtonIcon(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled by viewModel.propertyState("enabled"){ it?.toBoolean() ?: true }
    private val text by viewModel.propertyState("text")
    private val contentDescription by viewModel.propertyState("contentDescription")
    private val actions = node.propertyNodes("onClick")
    private val icon = node.property("icon")?.let {
        when (it) {
            "Columns" -> FontAwesomeIcons.Solid.Columns
            "Passport" -> FontAwesomeIcons.Solid.Passport
            "Socks" -> FontAwesomeIcons.Solid.Socks
            "Underline" -> FontAwesomeIcons.Solid.Underline
            "StreetView" -> FontAwesomeIcons.Solid.StreetView
            "CloudShowersHeavy" -> FontAwesomeIcons.Solid.CloudShowersHeavy
            "CarCrash" -> FontAwesomeIcons.Solid.CarCrash
            "FistRaised" -> FontAwesomeIcons.Solid.FistRaised
            "LaptopMedical" -> FontAwesomeIcons.Solid.LaptopMedical
            "AngleDoubleRight" -> FontAwesomeIcons.Solid.AngleDoubleRight
            "Clone" -> FontAwesomeIcons.Solid.Clone

            else -> error("Unknown value for imageVector: $it")
        }
    }

    @Composable
    override fun Content() {
        var isEnabled by remember { mutableStateOf(enabled ) }

        Button(modifier = modifier,
            enabled = isEnabled,
            onClick = {
                viewModel.invokeActions(
                    actions,
                    beforeInvoke = { isEnabled = false },
                    afterInvoke = { isEnabled = true }
                )
            }) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = contentDescription ?: text ?: ""
                    )
                }
                text?.let { Text(it) }
            }
        }
    }
}
