package me.developes.humming.sdui.navigation

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class JourneyDefinitionsTest {
    @Test fun identityRoutesResourcesAndReloadReachTheCache() = runTest {
        val cache = JourneyDefinitionCache(backgroundScope)
        val requests = mutableListOf<DefinitionKey>()
        val source = """{"id":"label","component":"layout:text","properties":{"text":"ready"}}"""
        fun definitions(session: String, version: String = "v1") = JourneyDefinitions("checkout", version, session, cache) {
            requests += it
            DefinitionSnapshot(it, source)
        }
        try {
            val a = definitions("a")
            a.load("home", "host", "home.json")
            a.load("home", "host", "home.json")
            assertEquals(1, requests.size)
            a.invalidate("home", "host", "home.json")
            a.load("home", "host", "home.json")
            definitions("b").load("home", "host", "home.json")
            definitions("a", "v2").load("home", "host", "home.json")
            a.load("other", "host", "home.json")
            a.load("home", "host", "other.json")
            assertEquals(6, requests.size)
            assertEquals(DefinitionKey("checkout", "v1", "a", "home", "host", "home.json"), requests.first())
            assertEquals(1, cache.metrics().hits)
        } finally { cache.close() }
    }
}
