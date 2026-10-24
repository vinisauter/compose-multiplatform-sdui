package me.developes.humming.sdui.common

/** Trusted client registry. Payloads select names; they cannot install formatting code. */
class PresentationTokens(colors: Map<String, String> = emptyMap(), masks: Map<String, AsciiDigitMask> = emptyMap()) {
    private val colors = colors.toMap()
    private val masks = masks.toMap()
    init {
        require(colors.values.all { color -> color.startsWith('#') && color.length in setOf(7, 9) &&
            color.drop(1).all { it in '0'..'9' || it.lowercaseChar() in 'a'..'f' } })
        require((colors.keys + masks.keys).all { it.isNotBlank() })
    }
    val colorNames: Set<String> get() = colors.keys.toSet()
    val maskNames: Set<String> get() = masks.keys.toSet()
    fun color(name: String): String? = colors[name]
    fun mask(name: String): AsciiDigitMask? = masks[name]
}

fun defaultPresentationTokens() = PresentationTokens(
    colors = mapOf("content" to "#202124", "positive" to "#146C43"),
    masks = mapOf("code" to AsciiDigitMask("###-###")),
)

class MaskPresentation internal constructor(val text: String, private val forward: IntArray, private val reverse: IntArray) {
    fun originalToTransformed(offset: Int): Int = forward[offset.coerceIn(0, forward.lastIndex)]
    fun transformedToOriginal(offset: Int): Int = reverse[offset.coerceIn(0, reverse.lastIndex)]
}

/** ASCII digit presentation only, without parsing numbers, truncating input or losing leading zeroes. */
class AsciiDigitMask(private val pattern: String) {
    init { require(pattern.length in 1..256 && '#' in pattern) }
    fun format(raw: String): MaskPresentation {
        if (raw.length > 4096 || raw.any { it !in '0'..'9' }) throw PayloadException(NodeDiagnostic(
            "MASK_INPUT", "Mask expects at most 4096 ASCII digits", "formatMask"))
        val output = StringBuilder()
        val forward = IntArray(raw.length + 1)
        val reverse = mutableListOf(0)
        var position = 0
        raw.forEachIndexed { index, digit ->
            while (position < pattern.length && pattern[position] != '#') {
                output.append(pattern[position++]); reverse += index
            }
            output.append(digit); reverse += index + 1
            if (position < pattern.length) position++
            forward[index + 1] = output.length
        }
        return MaskPresentation(output.toString(), forward, reverse.toIntArray())
    }
}
