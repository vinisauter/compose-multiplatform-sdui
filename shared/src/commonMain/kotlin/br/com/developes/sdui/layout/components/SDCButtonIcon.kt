package br.com.developes.sdui.layout.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.developes.sdui.SDCLibrary
import br.com.developes.sdui.SDCLibrary.Companion.launchHandling
import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.layout.Layout

class SDCButtonIcon(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private var modifier = Modifier.fromNode(node)
    private val enabled = node.propertyState("enabled", state)?.toBoolean()
    private val text = node.propertyState("text", state)
    private val contentDescription = node.propertyState("contentDescription", state)
    private val actions = node.propertyNodes("onClick")
    private val imageVector = node.property("imageVector")?.let {
        when (it) {
            "Add" -> Icons.Default.Add
            "AddCircle" -> Icons.Default.AddCircle
            "AccountCircle" -> Icons.Default.AccountCircle
            "AccountBox" -> Icons.Default.AccountBox
            "ArrowBack" -> Icons.AutoMirrored.Filled.ArrowBack
            "ArrowDropDown" -> Icons.Default.ArrowDropDown
            "ArrowForward" -> Icons.AutoMirrored.Filled.ArrowForward
            "Build" -> Icons.Default.Build
            "Call" -> Icons.Default.Call
            "Check" -> Icons.Default.Check
            "CheckCircle" -> Icons.Default.CheckCircle
            "Clear" -> Icons.Default.Clear
            "Close" -> Icons.Default.Close
            "Create" -> Icons.Default.Create
            "DateRange" -> Icons.Default.DateRange
            "Delete" -> Icons.Default.Delete
            "Done" -> Icons.Default.Done
            "Edit" -> Icons.Default.Edit
            "Email" -> Icons.Default.Email
            "ExitToApp" -> Icons.AutoMirrored.Filled.ExitToApp
            "Face" -> Icons.Default.Face
            "Favorite" -> Icons.Default.Favorite
            "FavoriteBorder" -> Icons.Default.FavoriteBorder
            "Home" -> Icons.Default.Home
            "Info" -> Icons.Default.Info
            "KeyboardArrowDown" -> Icons.Default.KeyboardArrowDown
            "KeyboardArrowLeft" -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
            "KeyboardArrowRight" -> Icons.AutoMirrored.Filled.KeyboardArrowRight
            "KeyboardArrowUp" -> Icons.Default.KeyboardArrowUp
            "List" -> Icons.AutoMirrored.Filled.List
            "LocationOn" -> Icons.Default.LocationOn
            "Lock" -> Icons.Default.Lock
            "MailOutline" -> Icons.Default.MailOutline
            "Menu" -> Icons.Default.Menu
            "MoreVert" -> Icons.Default.MoreVert
            "Notifications" -> Icons.Default.Notifications
            "Person" -> Icons.Default.Person
            "Phone" -> Icons.Default.Phone
            "Place" -> Icons.Default.Place
            "PlayArrow" -> Icons.Default.PlayArrow
            "Refresh" -> Icons.Default.Refresh
            "Search" -> Icons.Default.Search
            "Send" -> Icons.AutoMirrored.Filled.Send
            "Settings" -> Icons.Default.Settings
            "Share" -> Icons.Default.Share
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "Star" -> Icons.Default.Star
            "ThumbUp" -> Icons.Default.ThumbUp
            "Warning" -> Icons.Default.Warning
            else -> error("Unknown value for imageVector: $it")
        }
    }

    @Composable
    override fun Content() {
        var isEnabled by remember { mutableStateOf(enabled ?: true) }
        val action = SDCLibrary.loadActions(actions)
        // Creates a CoroutineScope bound to the Content's lifecycle
        val scope = rememberCoroutineScope()
        Button(modifier = modifier,
            enabled = isEnabled,
            onClick = {
                isEnabled = false
                scope.launchHandling(after = { isEnabled = true }) {
                    action.invoke(node, state)
                }
            }) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                imageVector?.let {
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
