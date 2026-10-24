package me.developes.humming.sdui.layout.components

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import me.developes.humming.sdui.common.*

val LocalPresentationTokens = staticCompositionLocalOf { defaultPresentationTokens() }

internal class DigitMaskTransformation(private val mask: AsciiDigitMask) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val result = try { mask.format(text.text) } catch (error: PayloadException) {
            // The component displays this input failure; keep every canonical character editable.
            return TransformedText(text, OffsetMapping.Identity)
        }
        return TransformedText(AnnotatedString(result.text), object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = result.originalToTransformed(offset)
            override fun transformedToOriginal(offset: Int) = result.transformedToOriginal(offset)
        })
    }
}
