package me.developes.humming.sdui.navigation

import me.developes.humming.sdui.common.ServerDrivenNode

/** Immutable identity for one mounted journey. The host owns the cache and its lifetime. */
class JourneyDefinitions(
    val journey: String,
    val version: String,
    val session: String,
    private val cache: JourneyDefinitionCache,
    private val loader: suspend (DefinitionKey) -> DefinitionSnapshot,
) {
    init { require(listOf(journey, version, session).all { it.isNotBlank() && it.length <= 4096 }) }

    private fun key(route: String, provider: String, resource: String) =
        DefinitionKey(journey, version, session, route, provider, resource)

    suspend fun load(route: String, provider: String, resource: String): ServerDrivenNode {
        val key = key(route, provider, resource)
        return cache.load(key) { loader(key) }
    }

    suspend fun invalidate(route: String, provider: String, resource: String) {
        cache.invalidate(key(route, provider, resource))
    }
}
