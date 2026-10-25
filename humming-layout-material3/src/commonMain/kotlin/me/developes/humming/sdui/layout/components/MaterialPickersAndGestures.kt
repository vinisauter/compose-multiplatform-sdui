@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.carousel.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import me.developes.humming.sdui.common.*

private fun date(value: String): Long? = value.toLongOrNull()?.takeIf { it in -2208988800000L..4133894400000L }
private fun time(value: String): Pair<Int, Int> {
    val parts = value.split(':')
    return (parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0) to
        (parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0)
}

@Composable internal fun SDCMaterial3.MaterialPickersAndGestures(change: () -> Unit, invoker: ActionInvoker) {
    val m = modifier.observeNodeBounds(node)
    when (name) {
        "datePicker" -> {
            val external = state("value", "")
            val picker = rememberDatePickerState(initialSelectedDateMillis = date(external))
            LaunchedEffect(external) { picker.selectedDateMillis = date(external) }
            LaunchedEffect(picker) { snapshotFlow { picker.selectedDateMillis }.drop(1).collect {
                if (it != date(vm.getState(raw("stateKey", "material3:${node.id}:value")))) {
                    write(it?.toString() ?: "", "value"); change()
                }
            } }
            DatePicker(picker, m, showModeToggle = bound("showModeToggle", "true").toBoolean())
        }
        "dateRangePicker" -> {
            val start = state("value", ""); val end = state("endValue", "", "endStateKey")
            val from = date(start); val to = date(end)?.takeIf { from != null && it >= from }
            val picker = rememberDateRangePickerState(initialSelectedStartDateMillis = from, initialSelectedEndDateMillis = to)
            LaunchedEffect(start, end) { picker.setSelection(from, to) }
            LaunchedEffect(picker) { snapshotFlow { picker.selectedStartDateMillis to picker.selectedEndDateMillis }.drop(1).collect {
                val stored = date(vm.getState(raw("stateKey", "material3:${node.id}:value"))) to
                    date(vm.getState(raw("endStateKey", "material3:${node.id}:endValue")))
                if (it != stored) { write(it.first?.toString() ?: "", "value"); write(it.second?.toString() ?: "", "endValue", "endStateKey"); change() }
            } }
            DateRangePicker(picker, m, showModeToggle = bound("showModeToggle", "true").toBoolean())
        }
        "timePicker", "timeInput" -> {
            val (hour, minute) = time(state("value", "00:00"))
            val is24Hour = bound("is24Hour", "true").toBoolean()
            val picker = rememberTimePickerState(hour, minute, is24Hour)
            var inputGeneration by remember { mutableIntStateOf(0) }
            LaunchedEffect(hour, minute, is24Hour) {
                // M3 1.8.2 TimeInput retains its own TextFieldValue. Reset its subtree only
                // for an external change, preserving focus/selection during user typing.
                if (picker.hour != hour || picker.minute != minute || picker.is24hour != is24Hour) {
                    picker.hour = hour; picker.minute = minute; picker.is24hour = is24Hour
                    inputGeneration++
                }
            }
            LaunchedEffect(picker) { snapshotFlow { picker.hour to picker.minute }.drop(1).collect {
                if (it != time(vm.getState(raw("stateKey", "material3:${node.id}:value")))) {
                    write("${it.first.toString().padStart(2, '0')}:${it.second.toString().padStart(2, '0')}", "value"); change()
                }
            } }
            if (name == "timePicker") TimePicker(picker, m) else key(inputGeneration) { TimeInput(picker, m) }
        }
        "swipeToDismissBox" -> {
            val external = state("value", "Settled")
            val target = SwipeToDismissBoxValue.entries.firstOrNull { it.name == external } ?: SwipeToDismissBoxValue.Settled
            val dismiss = rememberSwipeToDismissBoxState(target)
            LaunchedEffect(target) { if (dismiss.currentValue != target) {
                if (target == SwipeToDismissBoxValue.Settled) dismiss.reset() else dismiss.dismiss(target)
            } }
            LaunchedEffect(dismiss) { snapshotFlow { dismiss.currentValue }.drop(1).collect {
                if (it.name != vm.getState(raw("stateKey", "material3:${node.id}:value"))) { write(it.name, "value"); change() }
            } }
            SwipeToDismissBox(dismiss, { slot("backgroundContent") }, m, gesturesEnabled = bound("gesturesEnabled", "true").toBoolean()) { children() }
        }
        "pullToRefreshBox" -> {
            val refreshing = state("refreshing", "false").toBoolean()
            PullToRefreshBox(refreshing, {
                write("true", "refreshing"); invoker.invoke(node.propertyNodes("onRefresh"))
            }, m) { children() }
        }
        "horizontalMultiBrowseCarousel", "horizontalUncontainedCarousel" -> {
            val items = node.children.orEmpty()
            val carousel = rememberCarouselState { items.size }
            val width = number("itemWidth", 180f).dp; val spacing = number("itemSpacing", 0f).dp
            if (items.isNotEmpty()) {
                if (name == "horizontalMultiBrowseCarousel") HorizontalMultiBrowseCarousel(carousel, width, m.heightIn(min = 160.dp), itemSpacing = spacing) { index -> vm.loadComponent(items[index])?.Content() }
                else HorizontalUncontainedCarousel(carousel, width, m.heightIn(min = 160.dp), itemSpacing = spacing) { index -> vm.loadComponent(items[index])?.Content() }
            }
        }
        else -> error("Missing Material 3 adapter: $name")
    }
}
