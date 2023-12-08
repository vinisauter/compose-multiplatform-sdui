package me.next.serverdriven.core.library.layout.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import compose.icons.FeatherIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import me.next.serverdriven.compose.SDCLibrary
import me.next.serverdriven.core.library.interfaces.Layout
import me.next.serverdriven.core.tree.ServerDrivenNode
import kotlin.math.absoluteValue

class SDCTextField(val node: ServerDrivenNode, val state: MutableMap<String, String>) : Layout {
    private val modifier = Modifier.fromNode(node)
    private val onChangeUpdateState = node.property("onChangeUpdateState") ?: "TextField_${node.id}"
    private val text = node.property("text") ?: ""
    private val enabled = node.propertyState("enabled", state)?.toBoolean()
    private val readOnly = node.propertyState("readOnly", state)?.toBoolean()
    private val isError = node.propertyState("isError", state)?.toBoolean()
    private val autoCorrect = node.propertyState("autoCorrect", state)?.toBoolean()
    private val singleLine = node.property("singleLine")?.toBoolean()
    private val maxLines = node.property("maxLines")?.toInt()
    private val minLines = node.property("minLines")?.toInt()
    private val visualMask = node.property("visualMask")
    private val capitalization = node.property("capitalization")?.let {
        when (it) {
            "None" -> KeyboardCapitalization.None
            "Characters" -> KeyboardCapitalization.Characters
            "Words" -> KeyboardCapitalization.Words
            "Sentences" -> KeyboardCapitalization.Sentences
            else -> error("Unknown value for capitalization $it")
        }
    }
    private val keyboardType = node.property("keyboardType")?.let {
        when (it) {
            "Text" -> KeyboardType.Text
            "Ascii" -> KeyboardType.Ascii
            "Email" -> KeyboardType.Email
            "Number" -> KeyboardType.Number
            "NumberPassword" -> KeyboardType.NumberPassword
            "Decimal" -> KeyboardType.Decimal
            "Password" -> KeyboardType.Password
            "Phone" -> KeyboardType.Phone
            "Uri" -> KeyboardType.Uri
            else -> error("Unknown value for keyboardType $it")
        }
    }
    private val imeAction = node.property("imeAction")?.let {
        when (it) {
            "Default" -> ImeAction.Default
            "Done" -> ImeAction.Done
            "Go" -> ImeAction.Go
            "Next" -> ImeAction.Next
            "None" -> ImeAction.None
            "Previous" -> ImeAction.Previous
            "Search" -> ImeAction.Search
            "Send" -> ImeAction.Send
            else -> error("Unknown value for capitalization $it")
        }
    }
    private val onDone = node.propertyNodes("onDone")
    private val onGo = node.propertyNodes("onGo")
    private val onNext = node.propertyNodes("onNext")
    private val onPrevious = node.propertyNodes("onPrevious")
    private val onSearch = node.propertyNodes("onSearch")
    private val onSend = node.propertyNodes("onSend")

    private val label: @Composable (() -> Unit)? = node.propertyNodes("label").let {
        if (it.isEmpty()) return@let null
        return@let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }
        }
    }
    private val placeholder: @Composable (() -> Unit)? = node.propertyNodes("placeholder").let {
        if (it.isEmpty()) return@let null
        return@let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }
        }
    }
    private val leadingIcon: @Composable (() -> Unit)? = node.propertyNodes("leadingIcon").let {
        if (it.isEmpty()) return@let null
        return@let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }

        }
    }

    private val trailingIcon: @Composable (() -> Unit)? = node.propertyNodes("trailingIcon").let {
        if (it.isEmpty()) return@let null
        return@let {
            for (serverDrivenNode in it) {
                SDCLibrary.loadComponent(node = serverDrivenNode, dataState = state)
            }
        }
    }

    @Composable
    override fun Content() {
        if (state[onChangeUpdateState] == null) {
            state[onChangeUpdateState] = text
        }
        val onDone = SDCLibrary.loadActions(onDone)
        val onGo = SDCLibrary.loadActions(onGo)
        val onNext = SDCLibrary.loadActions(onNext)
        val onPrevious = SDCLibrary.loadActions(onPrevious)
        val onSearch = SDCLibrary.loadActions(onSearch)
        val onSend = SDCLibrary.loadActions(onSend)

        var passwordVisibility by remember { mutableStateOf(false) }
        val isPassword = keyboardType == KeyboardType.Password ||
                keyboardType == KeyboardType.NumberPassword
        val visualTransformation =
            visualMask?.let { MaskVisualTransformation(it) } ?: VisualTransformation.None
        val maxLength = visualMask?.count { it == '#' }
        TextField(
            value = state[onChangeUpdateState] ?: "",
            onValueChange = {
                if (maxLength == null || it.length <= maxLength)
                    state[onChangeUpdateState] = it
            },
            modifier = modifier,
            enabled = enabled ?: true,
            readOnly = readOnly ?: false,
            isError = isError ?: false,
            keyboardOptions = KeyboardOptions(
                capitalization = capitalization ?: KeyboardCapitalization.None,
                autoCorrect = autoCorrect ?: true,
                keyboardType = keyboardType ?: KeyboardType.Text,
                imeAction = imeAction ?: ImeAction.Default
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone.invoke(node, state) },
                onGo = { onGo.invoke(node, state) },
                onNext = { onNext.invoke(node, state) },
                onPrevious = { onPrevious.invoke(node, state) },
                onSearch = { onSearch.invoke(node, state) },
                onSend = { onSend.invoke(node, state) },
            ),
            singleLine = singleLine ?: false,
            maxLines = maxLines ?: if (singleLine == true) 1 else Int.MAX_VALUE,
            minLines = minLines ?: 1,
            visualTransformation = if (isPassword && !passwordVisibility) {
                PasswordVisualTransformation()
            } else visualTransformation,
            trailingIcon = trailingIcon ?: if (isPassword) {
                {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        // https://github.com/DevSrSouza/compose-icons/tree/master
                        val imageVector =
                            if (passwordVisibility) FeatherIcons.Eye else FeatherIcons.EyeOff
                        Icon(
                            imageVector,
                            contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                        )
                    }
                }
            } else null,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
//            textStyle = node.property("textStyle")?.let { LocalTextStyle.current },
//            interactionSource = interactionSource ?: remember { MutableInteractionSource() },
//            shape = shape ?: MaterialTheme.shapes.small.copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize),
//            colors = colors ?: TextFieldDefaults.textFieldColors()
        )
    }

    class MaskVisualTransformation(private val mask: String) : VisualTransformation {

        private val specialSymbolsIndices = mask.indices.filter { mask[it] != '#' }

        override fun filter(text: AnnotatedString): TransformedText {
            var out = ""
            var maskIndex = 0
            text.forEach { char ->
                while (specialSymbolsIndices.contains(maskIndex)) {
                    out += mask[maskIndex]
                    maskIndex++
                }
                out += char
                maskIndex++
            }
            return TransformedText(AnnotatedString(out), offsetTranslator())
        }

        private fun offsetTranslator() = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val offsetValue = offset.absoluteValue
                if (offsetValue == 0) return 0
                var numberOfHashtags = 0
                val masked = mask.takeWhile {
                    if (it == '#') numberOfHashtags++
                    numberOfHashtags < offsetValue
                }
                return masked.length + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                return mask.take(offset.absoluteValue).count { it == '#' }
            }
        }
    }
}