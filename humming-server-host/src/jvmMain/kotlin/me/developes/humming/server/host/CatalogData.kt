package me.developes.humming.server.host

import me.developes.humming.server.*
import kotlinx.serialization.json.*

/** Opt-in demo uses actual host clock/runtime, not a fake network or invented business data. */
fun catalogDataApplication(publications: PublicationApplication) = PublicationDataApplication(publications,
    mapOf("system" to RegisteredDataSource { context -> SourceData.Success(buildJsonObject {
        put("serverTime", System.currentTimeMillis()); put("runtime", System.getProperty("java.version"))
        put("actor", context.identity.actor.id)
    }.toString()) }), listOf(DataPlan("catalog-system", listOf(DataMapping("system", "system", mapOf(
        "serverTime" to DataField(listOf("serverTime"), DataKind.NUMBER),
        "runtime" to DataField(listOf("runtime"), DataKind.TEXT),
        "actor" to DataField(listOf("actor"), DataKind.TEXT)))))),
    DataSourceAuthorizer { _, source -> source == "system" }, DataClock(System::currentTimeMillis))
