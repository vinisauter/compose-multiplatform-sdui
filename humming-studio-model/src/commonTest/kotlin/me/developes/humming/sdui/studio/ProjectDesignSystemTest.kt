package me.developes.humming.sdui.studio

import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*
import kotlin.test.*

class ProjectDesignSystemTest {
    @Test fun legacyProjectRemainsV1AndThemedProjectUsesV2WithoutChangingNodes() {
        val codec = AppProjectCodec()
        val legacy = AppProject("App", ProjectViewport.PHONE, emptyMap(), LocalWorkspace.blankDocument())
        val oldJson = codec.encode(legacy).requireValue()
        assertEquals(JsonPrimitive(1), parseBoundedJson(oldJson).jsonObject["formatVersion"])
        assertFalse("designSystem" in parseBoundedJson(oldJson).jsonObject)
        assertEquals(legacy, codec.decode(oldJson).requireValue())
        val themed = legacy.copy(designSystem = DesignSystem(mode = ThemeMode.DARK))
        val newJson = codec.encode(themed).requireValue()
        assertEquals(JsonPrimitive(2), parseBoundedJson(newJson).jsonObject["formatVersion"])
        assertEquals(themed, codec.decode(newJson).requireValue())
        assertEquals(newJson, codec.encode(codec.decode(newJson).requireValue()).requireValue())
        assertEquals(legacy.document, codec.decode(newJson).requireValue().document)
        assertFalse(codec.decode(newJson.replace("\"formatVersion\":2", "\"formatVersion\":99")).isSuccess)
        assertFalse(codec.encode(themed.copy(designSystem = DesignSystem(spacing = emptyMap()))).isSuccess)
    }

    @Test fun compiledBundleCarriesThemeAndOldBundleOmitsTheNewField() {
        val contracts = LocalWorkspace().compile().requireValue()
        val codec = CompiledBundleCodec()
        val oldSource = codec.encode(contracts).requireValue()
        assertFalse("designSystem" in parseBoundedJson(oldSource).jsonObject.getValue("contracts").jsonObject)
        assertEquals(contracts, codec.decode(oldSource).requireValue())
        val themed = contracts.copy(designSystem = DesignSystem(mode = ThemeMode.LIGHT))
        val source = codec.encode(themed).requireValue()
        assertEquals(JsonPrimitive(2), parseBoundedJson(source).jsonObject["formatVersion"])
        assertEquals(themed, codec.decode(source).requireValue())
        assertEquals(contracts.artifacts, codec.decode(source).requireValue().artifacts)
        assertFalse(codec.decode(source.replaceFirst("\"formatVersion\":2", "\"formatVersion\":1")).isSuccess)
    }
}
