package me.developes.humming.sdui.common

import kotlin.test.*

class SduiValueTest {
    @Test fun stringsIdsNullAndMissingRemainDistinct() {
        assertEquals(SduiValue.Text("00042"),parseSduiValue("\"00042\""))
        assertEquals(SduiValue.Text("true"),parseSduiValue("\"true\""))
        assertEquals(SduiValue.BooleanValue(true),parseSduiValue("true"))
        assertEquals(SduiValue.Null,parseSduiValue("null"))
        assertNotEquals<SduiValue>(SduiValue.Missing,SduiValue.Null)
        assertNull(SduiValue.Missing.toLegacyText())
        assertEquals("null",SduiValue.Null.toLegacyText())
        assertEquals("",SduiValue.Text("").toLegacyText())
    }

    @Test fun numericLexemeIsPreservedWithoutIntegerOrPrecisionCoercion() {
        for (source in listOf("9007199254740993","-0","1.2300","1e-3")) {
            val value=parseSduiValue(source)
            assertEquals(SduiValue.NumberValue(source),value)
            assertEquals(source,value.toLegacyText())
        }
        val error=assertFailsWith<PayloadException> { parseSduiValue("""{"value":1e999}""") }
        assertEquals("VALUE_NUMBER",error.diagnostic.code)
        assertEquals("$[\"value\"]",error.diagnostic.path)
        for (source in listOf("NaN","Infinity","01","+1","1."))
            assertFailsWith<IllegalArgumentException> { SduiValue.NumberValue(source) }
    }

    @Test fun collectionsCopyTheirInputsAndRoundtripNestedValues() {
        val source=mutableMapOf<String,SduiValue>("id" to SduiValue.Text("07"))
        val obj=SduiValue.ObjectValue(source)
        source.clear()
        assertEquals(SduiValue.Text("07"),obj.values["id"])
        val values=mutableListOf<SduiValue>(obj,SduiValue.Null)
        val array=SduiValue.ArrayValue(values)
        values.clear()
        assertEquals(array,parseSduiValue(array.toJsonValue().toString()))
        assertFailsWith<IllegalArgumentException> { SduiValue.ArrayValue(listOf(SduiValue.Missing)) }
        assertFailsWith<IllegalArgumentException> { SduiValue.ObjectValue(mapOf("x" to SduiValue.Missing)) }
        assertFailsWith<IllegalStateException> { SduiValue.Missing.toJsonValue() }
        assertFailsWith<PayloadException> { parseSduiValue("[1,2]",PayloadLimits(maxJsonValues=2)) }
    }
}
