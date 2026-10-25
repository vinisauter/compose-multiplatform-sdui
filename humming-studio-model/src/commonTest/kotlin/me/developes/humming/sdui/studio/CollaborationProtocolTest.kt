package me.developes.humming.sdui.studio

import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.initialNodeCatalog
import kotlin.test.*

class CollaborationProtocolTest {
    @Test fun feedCodecOwnsThePlacementDiscriminatorAndRequiresAnExplicitVersion() {
        val catalog = initialNodeCatalog()
        val document = LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"root","component":"layout:column","children":[{"id":"child","component":"layout:text","properties":{"text":"initial"}}]}""", "document").requireValue()
        val changed = EditSession(document, catalog).execute(EditCommand("edit", 0,
            listOf(EditOperation.PutProperty("child", "text", JsonPrimitive("changed")))))
        val feed = DraftUpdateFeed("document", 0, 1, listOf(DraftUpdate(1, "edit", "author", changed.forward!!)), false, false)
        val codec = DraftUpdateFeedCodec(); val source = codec.encode(feed).requireValue()
        assertTrue(source.contains("\"operationType\":\"child\""))
        assertEquals(feed, codec.decode(source).requireValue())
        assertFalse(codec.decode(Json.encodeToString(DraftUpdateFeed.serializer(), feed)).isSuccess)
        val objectValue = Json.parseToJsonElement(source).jsonObject
        assertFalse(codec.decode(JsonObject(objectValue - "formatVersion").toString()).isSuccess)
        assertFalse(codec.decode(source.replace("\"formatVersion\":1", "\"formatVersion\":2")).isSuccess)
    }
}
