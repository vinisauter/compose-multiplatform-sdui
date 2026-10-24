package sample.app

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.awaitCancellation
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.navigation.SDNavigation
import me.developes.humming.sdui.studio.LocalWorkspace
import org.junit.Rule
import org.junit.Test
import kotlin.test.*

class ScreenIsolationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun studioIsolationExampleStartsFreshAndReturnsToTheEditedValue() {
        val host=object : LifecycleOwner { override val lifecycle=LifecycleRegistry(this) }
        var mounted by mutableStateOf(true)
        compose.runOnIdle { host.lifecycle.currentState=Lifecycle.State.RESUMED }
        compose.setContent { if (mounted) CompositionLocalProvider(LocalLifecycleOwner provides host) {
            LocalStudio(workspace=remember { LocalWorkspace(isolationExample()) })
        } }
        try {
            awaitText("Valor: inicial")
            compose.onNode(hasSetTextAction() and hasText("inicial")).performTextReplacement("Primeira visita")
            awaitText("Valor: Primeira visita")
            compose.onNodeWithText("Nova visita").performClick()
            awaitText("Valor: inicial")
            compose.onNode(hasSetTextAction() and hasText("inicial")).performTextReplacement("Segunda visita")
            awaitText("Valor: Segunda visita")
            compose.onNodeWithText("Voltar à visita anterior").performClick()
            awaitText("Valor: Primeira visita")
        } finally {
            compose.runOnIdle { mounted=false }
            compose.waitForIdle()
            compose.runOnIdle { host.lifecycle.currentState=Lifecycle.State.DESTROYED }
        }
    }

    @Test fun repeatedRouteHasIndependentStateReturnRestoresPreviousEntryAndPopCancelsTasks() {
        val host = object : ViewModelStoreOwner,LifecycleOwner {
            override val viewModelStore=ViewModelStore()
            override val lifecycle=LifecycleRegistry(this)
        }
        val entries=linkedMapOf<String,SDUIFormViewModel>()
        val started=mutableSetOf<String>()
        val cancelled=mutableSetOf<String>()
        var mounted by mutableStateOf(true)
        lateinit var root: SDUIFormViewModel
        val graph="""{"component":"navigation:graph","properties":{"startDestination":"form"},"children":[{"component":"navigation:node","properties":{"name":"form","type":"local","destiny":"form"}},{"component":"navigation:node","properties":{"name":"broken","type":"absent","destiny":"missing"}}]}"""
        val form="""{"component":"layout:column","children":[{"component":"action:event","properties":{"onCreate":{"component":"action:method","properties":{"invoke":"capture"}}}},{"component":"layout:textField","properties":{"text":"initial","onChangeUpdateState":"field"}},{"component":"layout:text","properties":{"text":"Value: #{field}"}},{"component":"layout:button","properties":{"onClick":{"component":"navigation:goTo","properties":{"destiny":"form"}}},"children":[{"component":"layout:text","properties":{"text":"Push same"}}]},{"component":"layout:button","properties":{"onClick":{"component":"action:method","properties":{"invoke":"hold"}}},"children":[{"component":"layout:text","properties":{"text":"Hold task"}}]},{"component":"layout:button","properties":{"onClick":{"component":"action:method","properties":{"invoke":"missing"}}},"children":[{"component":"layout:text","properties":{"text":"Entry error"}}]},{"component":"layout:button","properties":{"onClick":{"component":"navigation:goTo","properties":{"destiny":"broken"}}},"children":[{"component":"layout:text","properties":{"text":"Missing provider"}}]},{"component":"layout:button","properties":{"onClick":{"component":"navigation:goBack"}},"children":[{"component":"layout:text","properties":{"text":"Back"}}]}]}"""
        val config=HummingSDUI.Config("local","graph",theme=Material3Theme(),
            screenStateMode=ScreenStateMode.ISOLATED_ENTRIES,
            entryInputs={ id,route -> ScreenInputs(
                server=SduiValue.ObjectValue(mapOf("id" to SduiValue.Text("0007"))),
                nav=SduiValue.ObjectValue(mapOf("entry" to SduiValue.Text(id),"route" to SduiValue.Text(route)))) },
            errorHandler={ error,owner ->
                Text("Screen failure: ${error.code}")
                TextButton(onClick=owner::dismissError) { Text("Dismiss entry error") }
            }) {
            root=this
            updateState("field","host-only")
            addLibrary(SDActions().registerMethod("capture") { _,owner -> entries[checkNotNull(owner.screenEntryId)]=owner }
                .registerMethod("hold") { _,owner -> owner.runTask({
                    val id=checkNotNull(owner.screenEntryId)
                    started+=id
                    try { awaitCancellation() } finally { cancelled+=id }
                }) })
            addLibrary(SDLayoutMaterial3()); addLibrary(SDNavigation())
            addNodeProvider("local") { path -> parseNode(if (path == "graph") graph else form,documentId=path) }
        }
        compose.runOnIdle { host.lifecycle.currentState=Lifecycle.State.RESUMED }
        compose.setContent { if (mounted) CompositionLocalProvider(LocalViewModelStoreOwner provides host,LocalLifecycleOwner provides host) {
            HummingSDUI().Content(config,savedStateHandleFactory={ SavedStateHandle() })
        } }
        try {
            awaitText("Value: initial")
            compose.waitUntil(10000) { entries.size == 1 }
            val first=entries.values.single()
            compose.onNode(hasSetTextAction()).performTextReplacement("Alpha")
            awaitText("Value: Alpha")
            compose.onNodeWithText("Push same").performClick()
            compose.waitUntil(10000) { entries.size == 2 }
            awaitText("Value: initial")
            val second=entries.values.last()
            assertNotSame(first,second)
            assertNotEquals(first.screenEntryId,second.screenEntryId)
            compose.onNode(hasSetTextAction()).performTextReplacement("Beta")
            awaitText("Value: Beta")
            compose.onNodeWithText("Entry error").performClick()
            awaitText("Screen failure: MISSING_METHOD")
            compose.runOnIdle { assertNull(root.errorState.value); assertNull(first.errorState.value) }
            compose.onNodeWithText("Dismiss entry error").performClick()
            compose.onNodeWithText("Missing provider").performClick()
            awaitText("Screen failure: MISSING_PROVIDER")
            compose.runOnIdle { assertNull(root.errorState.value); assertNull(first.errorState.value) }
            compose.onNodeWithText("Dismiss entry error").performClick()
            compose.onNodeWithText("Hold task").performClick()
            compose.waitUntil(10000) { second.screenEntryId in started }
            compose.onNodeWithText("Back").performClick()
            awaitText("Value: Alpha")
            compose.waitUntil(10000) { second.screenEntryId in cancelled }
            compose.runOnIdle {
                assertEquals("Alpha",first.formState["field"])
                assertEquals("host-only",root.formState["field"])
                assertTrue(second.stateValues.isEmpty())
                assertTrue(second.screenInputs.server.values.isEmpty())
                assertEquals(SduiValue.Text("0007"),first.screenInputs.server.values["id"])
            }
            compose.onNodeWithText("Push same").performClick()
            compose.waitUntil(10000) { entries.size == 3 }
            awaitText("Value: initial")
            assertNotEquals(second.screenEntryId,entries.values.last().screenEntryId)
        } finally {
            compose.runOnIdle { mounted=false }
            compose.waitForIdle()
            compose.runOnIdle { host.lifecycle.currentState=Lifecycle.State.DESTROYED; host.viewModelStore.clear() }
        }
        compose.runOnIdle { assertTrue(entries.values.all { it.stateValues.isEmpty() }) }
    }

    private fun awaitText(text: String) { compose.waitUntil(10000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 } }
}
