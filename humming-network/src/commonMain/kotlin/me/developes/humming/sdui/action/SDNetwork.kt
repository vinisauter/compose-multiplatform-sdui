package me.developes.humming.sdui.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.common.SDLibrary

class SDNetwork : SDLibrary("network") {

    init {
        addAction("get") { node, vm ->
            node.property("invoke")?.run {
//                http://myapi/graphql?query={me{name}}
//                {
//                    me {
//                        name
//                    }
//                }
//                {
//                    "data": { ... },
//                    "errors": [ ... ],
//                    "extensions": { ... }
//                }
                val url = vm.resolveStatePlaceholders(node.property("url"))
                val client = HttpClient()
                val response = client.get(url)
                val json = response.bodyAsText()

            }
        }
        addAction("post") { node, vm ->
            node.property("invoke")?.run {
                val url = vm.resolveStatePlaceholders(node.property("url"))
                val client = HttpClient()
                val response = client.get(url)
                val json = response.bodyAsText()

//                {
//                    "query": "...",
//                    "operationName": "...",
//                    "variables": { "myVariable": "someValue", ... },
//                    "extensions": { "myExtension": "someValue", ... }
//                }
            }
        }

        addComponent("request") { node, vm ->
            object : ComposableContent {
                @Composable
                override fun Content() {
                }
            }
        }
    }
}