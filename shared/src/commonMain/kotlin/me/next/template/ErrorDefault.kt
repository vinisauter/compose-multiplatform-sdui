package me.next.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun ErrorDefault(
    throwable: Throwable,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.background(Color.Red)) {
        Text("Error ${throwable.message}")
    }
}