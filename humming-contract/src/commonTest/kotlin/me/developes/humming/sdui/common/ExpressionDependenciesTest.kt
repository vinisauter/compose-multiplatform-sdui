package me.developes.humming.sdui.common

import kotlin.test.*

class ExpressionDependenciesTest {
    private fun dependencies(source: String) = compileExpression(ExpressionSpec("humming.expression", 1, source)).dependencies

    @Test fun nestedStaticReadsObserveOnlyTheirRootKeysIncludingAllConditionalBranches() {
        val deps = dependencies("state.profile.name + state[\"suffix\"] + (false && state.hidden)")
        assertEquals(setOf("profile", "suffix", "hidden"), deps.keys("state"))
        assertFalse(deps.readsAll("state"))
        assertFalse(deps.readsScope("server"))
    }

    @Test fun dynamicRootAccessAndWholeScopeReadsAreConservative() {
        val dynamic = dependencies("state[nav.key] ?? state.fallback")
        assertTrue(dynamic.readsAll("state"))
        assertEquals(setOf("key"), dynamic.keys("nav"))
        assertTrue(dependencies("state").readsAll("state"))
        assertTrue(dependencies("state.fallback ?? state[nav.key]").readsAll("state"))
    }

    @Test fun dynamicNestedAccessDoesNotObserveUnrelatedRootKeys() {
        val deps = dependencies("state.items[state.index].name ?? server.default")
        assertEquals(setOf("items", "index"), deps.keys("state"))
        assertEquals(setOf("default"), deps.keys("server"))
        assertFalse(deps.readsAll("state"))
        assertFalse(dependencies("1 + 2").readsScope("state"))
    }
}
