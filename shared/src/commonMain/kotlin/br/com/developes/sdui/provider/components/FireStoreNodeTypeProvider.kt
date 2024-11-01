package br.com.developes.sdui.provider.components

import br.com.developes.sdui.ServerDrivenNode
import br.com.developes.sdui.provider.NodeTypeProvider
import br.com.developes.sdui.utils.toNode
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * reads a JSON file from the specified resource, parses it into a [JsonObject],
 * and converts it into a [ServerDrivenNode] which represents the root of the server-driven UI.
 * This [ServerDrivenNode] is then accessible through the node property.
 * */
class FirestoreNodeTypeProvider(
    firebaseFirestore: FirebaseFirestore,
    firestorePath: String
) : NodeTypeProvider {
    override val node: ServerDrivenNode
    val path = firestorePath.split("/")

    init {
        val json = runBlocking {
            val dictionaryRef = firebaseFirestore.collection(path[0])
                .document(path[1])
            dictionaryRef.get().let { dictionary ->
                if (dictionary.exists) {
                    val translationItems = dictionary.data<HashMap<String, String>>()
                    translationItems[path[2]]
                } else {
                    error("Firestore Item does not exist! ${path[0]} / ${path[1]} / ${path[2]}")
                }
            }
        }
        node = Json.decodeFromString<JsonObject>(json!!).toNode()
    }
}