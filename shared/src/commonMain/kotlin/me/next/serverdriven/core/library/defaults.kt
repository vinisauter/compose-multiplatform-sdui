package me.next.serverdriven.core.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
var defaultLoading: @Composable (modifier: Modifier) -> Unit = {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(modifier = it)
    }
}

@ThreadLocal
var defaultError: @Composable (modifier: Modifier, throwable: Throwable) -> Unit =
    { modifier, throwable ->
        Column(modifier = modifier.background(Color.Red)) {
            Text("Error: ${throwable.message?:throwable::class.simpleName}")
        }
    }