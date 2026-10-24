package me.developes.humming.sdui.common

import kotlin.test.*

class PresentationTokensTest {
    @Test fun masksPreserveLeadingZerosOverflowAndMonotoneCursorMappings() {
        val mask = AsciiDigitMask("(##)-##")
        assertEquals("", mask.format("").text)
        assertEquals("(0", mask.format("0").text)
        val raw = "0012345"
        val result = mask.format(raw)
        assertEquals("(00)-12345", result.text)
        for (offset in 0..raw.length) {
            val transformed = result.originalToTransformed(offset)
            assertEquals(offset, result.transformedToOriginal(transformed))
            if (offset > 0) assertTrue(transformed > result.originalToTransformed(offset - 1))
        }
        for (offset in 1..result.text.length) assertTrue(result.transformedToOriginal(offset) >= result.transformedToOriginal(offset - 1))
        assertEquals(0, result.transformedToOriginal(-1))
        assertEquals(raw.length, result.transformedToOriginal(Int.MAX_VALUE))
    }

    @Test fun untrustedInputAndExcessiveFormattingWorkAreRejectedWithoutNumericConversion() {
        val mask = AsciiDigitMask("###-###")
        assertEquals("MASK_INPUT", assertFailsWith<PayloadException> { mask.format("00A123") }.diagnostic.code)
        assertFailsWith<PayloadException> { mask.format("0".repeat(4097)) }
        assertFailsWith<IllegalArgumentException> { AsciiDigitMask("no slots") }
    }

    @Test fun tokenRegistryCopiesInputsAndOnlyExposesInstalledNames() {
        val colors = mutableMapOf("content" to "#202124")
        val tokens = PresentationTokens(colors)
        colors["content"] = "bad"
        assertEquals("#202124", tokens.color("content"))
        assertNull(tokens.color("unknown"))
        assertTrue(tokens.maskNames.isEmpty())
        assertFailsWith<IllegalArgumentException> { PresentationTokens(mapOf("content" to "arbitrary")) }
    }
}
