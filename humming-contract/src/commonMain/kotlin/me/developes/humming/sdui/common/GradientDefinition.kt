package me.developes.humming.sdui.common

import kotlinx.serialization.json.*

/** Finite declarative brush; no executable callbacks or platform objects. */
internal fun validGradient(value: JsonElement): Boolean {
    val objectValue = value as? JsonObject ?: return false
    fun content(key: String) = (objectValue[key] as? JsonPrimitive)?.contentOrNull
    if (content("type") !in setOf("linear", "radial", "sweep", "vertical", "horizontal")) return false
    if (objectValue.keys.any { it !in setOf("type", "colors", "tileMode", "startOffset", "endOffset", "centerOffset", "radius", "startY", "endY", "startX", "endX") }) return false
    val colors = objectValue["colors"] as? JsonArray ?: return false
    if (colors.size < 2 || colors.any { color ->
        val hex = (color as? JsonPrimitive)?.contentOrNull?.removePrefix("#") ?: return@any true
        hex.length !in setOf(3, 4, 6, 8) || hex.any { it !in "0123456789abcdefABCDEF" }
    }) return false
    if ("tileMode" in objectValue && content("tileMode") !in setOf("Clamp", "Repeat", "Mirror", "Decal")) return false
    for (key in listOf("startOffset", "endOffset", "centerOffset")) if (key in objectValue) {
        val offset = objectValue[key] as? JsonObject ?: return false
        if (offset.keys != setOf("x", "y") || offset.values.any { (it as? JsonPrimitive)?.floatOrNull?.isFinite() != true }) return false
    }
    for (key in listOf("radius", "startY", "endY", "startX", "endX")) if (key in objectValue) {
        val number = (objectValue[key] as? JsonPrimitive)?.floatOrNull ?: return false
        if (!number.isFinite() || key == "radius" && number <= 0f) return false
    }
    return true
}

internal val filterBlendModes = setOf("SrcOver", "DstOver", "SrcIn", "DstIn", "SrcOut", "DstOut", "SrcAtop", "DstAtop",
    "Xor", "Plus", "Modulate", "Screen", "Overlay", "Darken", "Lighten", "ColorDodge", "ColorBurn", "HardLight", "SoftLight",
    "Difference", "Exclusion", "Multiply", "Hue", "Saturation", "Color", "Luminosity")

internal fun validColorFilter(value: JsonElement): Boolean {
    val filter = value as? JsonObject ?: return false
    fun color(key: String): Boolean {
        val hex = (filter[key] as? JsonPrimitive)?.contentOrNull?.removePrefix("#") ?: return false
        return hex.length in setOf(3, 4, 6, 8) && hex.all { it in "0123456789abcdefABCDEF" }
    }
    return when ((filter["type"] as? JsonPrimitive)?.contentOrNull) {
        "tint" -> filter.keys.all { it in setOf("type", "color", "blendMode") } && color("color") &&
            ("blendMode" !in filter || (filter["blendMode"] as? JsonPrimitive)?.contentOrNull in filterBlendModes)
        "lighting" -> filter.keys == setOf("type", "mul", "add") && color("mul") && color("add")
        "colorMatrix" -> filter.keys == setOf("type", "matrix") && (filter["matrix"] as? JsonArray)?.let { matrix ->
            matrix.size == 20 && matrix.all { (it as? JsonPrimitive)?.floatOrNull?.isFinite() == true }
        } == true
        else -> false
    }
}
