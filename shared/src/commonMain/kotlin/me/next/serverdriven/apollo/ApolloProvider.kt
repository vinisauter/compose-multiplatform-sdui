package me.next.serverdriven.apollo

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.tree.ServerDrivenNode
import me.next.serverdriven.graphql.CreateNodeMutation
import me.next.serverdriven.graphql.GetNodeQuery
import me.next.serverdriven.graphql.HealthQuery
import me.next.serverdriven.graphql.LogInMutation
import me.next.serverdriven.graphql.SignUpMutation
import me.next.serverdriven.graphql.ViewerQuery
import me.next.serverdriven.settings.createKeyValueSettings
import me.next.serverdriven.utils.JsonServerDrivenNode
import me.next.serverdriven.utils.toJsonElement


class ApolloProvider {
    private val serverUrl = "https://parseapi.back4app.com/graphql"
    private val parseAppId = "YOUR-APP-ID"
    private val parseClientKey = "YOUR-CLIENT-KEY"
    private val back4AppInterceptor = ApolloBack4AppInterceptor(parseAppId, parseClientKey)
    private val settings = createKeyValueSettings("back4app.graphql")
    var sessionToken: String? = null
        set(value) {
            back4AppInterceptor.sessionToken = value
            field = value
        }
    private val apolloClient: ApolloClient = ApolloClient.Builder()
        .serverUrl(serverUrl)
        .addHttpInterceptor(LogInterceptor())
        .addHttpInterceptor(back4AppInterceptor)
        .build()

    suspend fun health(): Boolean {
        val response = apolloClient.query(HealthQuery()).execute()
        return response.dataAssertNoErrors.health
    }

    suspend fun getNode(nodeId: String): ServerDrivenNode? {
        val response = apolloClient.query(GetNodeQuery(nodeId)).execute()
        return response.dataAssertNoErrors.sDNodes.edges?.firstOrNull()?.node?.toNode()
    }

    suspend fun signUp(
        username: String,
        password: String,
        email: String,
    ) = apolloClient.mutation(
        SignUpMutation(username, password, email)
    ).execute().let { signUp ->
        try {
            signUp.dataAssertNoErrors.signUp!!.also {
                sessionToken = it.viewer.sessionToken
            }
        } catch (error: Throwable) {
            error("${signUp.errors?.get(0)?.message ?: error::class.simpleName}")
        }
    }


    suspend fun login(
        username: String,
        password: String
    ) = apolloClient.mutation(
        LogInMutation(username, password)
    ).execute().let { login ->
        try {
            login.dataAssertNoErrors.logIn!!.also {
                sessionToken = it.viewer.sessionToken
            }
        } catch (error: Throwable) {
            error("${login.errors?.get(0)?.message ?: error::class.simpleName}")
        }
    }

    suspend fun currentViewer() = apolloClient.query(
        ViewerQuery()
    ).execute().let { current ->
        try {
            current.dataAssertNoErrors.viewer.also {
                sessionToken = it.sessionToken
            }
        } catch (error: Throwable) {
            null
        }
    }

    suspend fun createNode(jsonFile: String) {
        val upload = SDCLibrary.loadNodeTypeProvider("file")
            .invoke(jsonFile) as JsonServerDrivenNode
        return upload.run {
            apolloClient.mutation(
                CreateNodeMutation(
                    id,
                    component,
                    properties.opt(),
                    children?.map { it.toJsonElement() }.opt()
                )
            ).execute().dataAssertNoErrors
        }
    }

    private fun <T> T?.opt(): Optional<T> {
        return this?.let { Optional.present(this) } ?: Optional.absent()
    }

    suspend fun init() {
        settings.getStringOrNull("sessionToken")?.run {
            sessionToken = this
        }
        if (sessionToken == null) {
            sessionToken = try {
                login(
                    "johndoe",
                    "ASuperStrongPassword"
                ).viewer.sessionToken
            } catch (t: Throwable) {
                signUp(
                    "johndoe",
                    "ASuperStrongPassword",
                    "johndoe@hotmail.com"
                ).viewer.sessionToken
            }
            settings.putString("sessionToken", sessionToken!!)
        }
    }
}

private fun GetNodeQuery.Node?.toNode(): ServerDrivenNode? {
    return this?.run {

        ServerDrivenNode(
            id = nodeId,
            component = component,
            properties = properties?.let { map ->
                return@let (map as Map<*, *>)
                    .mapKeys { it.key.toString() }
                    .mapValues { it.value?.toString() }
                    .toMutableMap()
            },
            children = children?.let {
                val list = ArrayList<ServerDrivenNode>()
                for (child in it) {
                    when (val jsonElement = child?.onElement?.value) {
                        is JsonObject -> list.add(JsonServerDrivenNode(jsonElement))
                        is JsonArray -> for (json in jsonElement) {
                            list.add(JsonServerDrivenNode(json as JsonObject))
                        }

                        else -> error("Unknown type for $jsonElement")
                    }
                }
                return@let list
            }
        )
    }
}
