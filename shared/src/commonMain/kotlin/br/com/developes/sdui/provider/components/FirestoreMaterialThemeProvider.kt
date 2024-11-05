package br.com.developes.sdui.provider.components

import br.com.developes.sdui.MaterialThemeNode
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class FirestoreMaterialThemeProvider(
    firebaseFirestore: FirebaseFirestore,
    firestorePath: String
) {
    val result: MaterialThemeNode
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
        result = Json.decodeFromJsonElement(
            MaterialThemeNode.serializer(),
            Json.decodeFromString<JsonObject>(json!!)
        )
    }
}