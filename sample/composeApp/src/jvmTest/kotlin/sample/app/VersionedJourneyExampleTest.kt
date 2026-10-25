package sample.app

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.Rule
import org.junit.Test

class VersionedJourneyExampleTest {
    @get:Rule val compose = createComposeRule()

    @Test fun localHostNavigatesRestoresDefinitionRecoversAndChangesSession() {
        val host = object : LifecycleOwner { override val lifecycle = LifecycleRegistry(this) }
        var mounted by mutableStateOf(true)
        compose.runOnIdle { host.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.setContent {
            if (mounted) CompositionLocalProvider(LocalLifecycleOwner provides host) { VersionedJourneyExample() }
        }
        fun awaitText(text: String) {
            try { compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().size == 1 } }
            catch (failure: Throwable) { println(compose.onRoot().printToString()); throw failure }
        }
        try {
            awaitText("Nome: inicial")
            compose.onNode(hasSetTextAction()).performTextReplacement("Ana")
            awaitText("Nome: Ana")
            compose.onNodeWithText("Abrir detalhe").performClick()
            awaitText("Detalhe carregado")
            compose.onNodeWithText("Voltar ao formulário").performClick()
            awaitText("Nome: Ana")
            compose.onNodeWithText("Restaurar definições").performClick()
            awaitText("Nome: inicial")
            awaitText("Fontes locais: 0 · snapshots restaurados: 1")
            compose.onNodeWithText("Falhar próxima carga").performClick()
            compose.onNodeWithText("Recarregar início").performClick()
            awaitText("Falha: DEFINITION_UNAVAILABLE")
            awaitText("Nome: inicial")
            compose.onNodeWithText("Recuperar início").performClick()
            awaitText("Fontes locais: 0 · snapshots restaurados: 2")
            compose.onNodeWithText("Trocar sessão").performClick()
            awaitText("Jornada local — sessão 2")
            awaitText("Nome: inicial")
            awaitText("Fontes locais: 1 · snapshots restaurados: 0")
        } finally {
            compose.runOnIdle { mounted = false }
            compose.waitForIdle()
            compose.runOnIdle { host.lifecycle.currentState = Lifecycle.State.DESTROYED }
        }
    }
}
