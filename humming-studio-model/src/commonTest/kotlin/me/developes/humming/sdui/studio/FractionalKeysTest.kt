package me.developes.humming.sdui.studio

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FractionalKeysTest {
    @Test fun referenceVectorsMatchStudioSpecification() {
        for ((previous, next, expected) in listOf(
            Triple(null, null, "V"), Triple("V", null, "k"), Triple(null, "V", "F"),
            Triple("V", "W", "VV"), Triple("V", "V0V", "V0F"),
        )) assertEquals(expected, generateKeyBetween(previous, next))
    }

    @Test fun deterministicRandomInsertionsStayStrictlyBetweenNeighborsWithoutRenumbering() {
        val random = Random(73014)
        val keys = mutableListOf<String>()
        repeat(2_000) {
            val index = random.nextInt(keys.size + 1)
            val before = keys.toList()
            val previous = keys.getOrNull(index - 1)
            val next = keys.getOrNull(index)
            val generated = generateKeyBetween(previous, next)
            assertTrue(validOrderKey(generated, 256))
            assertTrue(previous == null || previous < generated)
            assertTrue(next == null || generated < next)
            keys.add(index, generated)
            assertEquals(before, keys.filterIndexed { position, _ -> position != index })
        }
        assertEquals(keys.size, keys.toSet().size)
        assertEquals(keys.sorted(), keys)
    }

    @Test fun repeatedInsertionIntoOneIntervalAndPrefixBoundsRemainValid() {
        var upper = "W"
        repeat(500) {
            val key = generateKeyBetween("V", upper)
            assertTrue("V" < key && key < upper)
            upper = key
        }
        for (next in listOf("0001", "0V", "VV", "zzzzV")) {
            val key = generateKeyBetween(null, next)
            assertTrue(key < next)
            assertTrue(validOrderKey(key, 256))
        }
    }

    @Test fun invalidKeysAndNeighborsAreRejected() {
        for (key in listOf("", "V0", "-", "á", "a b")) {
            assertEquals("INVALID_ORDER_KEY", assertFailsWith<OrderKeyException> { generateKeyBetween(key, null) }.code)
        }
        for ((previous, next) in listOf("V" to "V", "W" to "V")) assertEquals(
            "INVALID_KEY_NEIGHBORS", assertFailsWith<OrderKeyException> { generateKeyBetween(previous, next) }.code)
    }

    @Test fun exhaustionReportsAnErrorInsteadOfTruncatingOrChangingExistingKeys() {
        assertEquals("KEY_SPACE_EXHAUSTED", assertFailsWith<OrderKeyException> { generateKeyBetween("V", "W", 1) }.code)
        assertEquals("INVALID_ORDER_KEY", assertFailsWith<OrderKeyException> { generateKeyBetween("VV", null, 1) }.code)
        assertEquals("V", generateKeyBetween(null, null, 1))
    }
}
