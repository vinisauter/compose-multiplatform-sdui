package me.developes.humming.sdui.layout.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import me.developes.humming.sdui.common.collectAsState
import me.developes.humming.sdui.common.rememberActionInvoker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import me.developes.humming.sdui.common.SDUIFormViewModel
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.toColor
import me.developes.humming.sdui.layout.LayoutDefinitions
import me.developes.humming.sdui.layout.Layout
import kotlin.math.absoluteValue

class SDCTextField(val node: ServerDrivenNode, val viewModel: SDUIFormViewModel) : Layout {
    private val properties = LayoutDefinitions.textField.parse(node).requireValid()

    private val modifier = Modifier.fromNode(node)
    private val onChangeUpdateState = properties.property("onChangeUpdateState") ?: "TextField_${node.id}"
    private val text = viewModel.stateBinding(onChangeUpdateState, properties.property("text"))
    private val enabled = viewModel.binding(properties.property("enabled")) {
        it?.toBooleanStrictOrNull() ?: true
    }
    private val isError = viewModel.binding(properties.property("isError")) {
        it.toBoolean()
    }
    private val readOnly = properties.property("readOnly")?.toBoolean() ?: false
    private val autoCorrect = properties.property("autoCorrect")?.toBoolean() ?: true
    private val fontSize = properties.property("fontSize")?.toFloatOrNull()?.sp ?: 16.sp
    private val color = properties.property("color")
    private val backgroundColor = properties.property("backgroundColor")
    private val singleLine = properties.property("singleLine")?.toBoolean()
    private val maxLines = properties.property("maxLines")?.toInt()
    private val minLines = properties.property("minLines")?.toInt()
    private val textFieldColor = properties.property("textFieldColor")?.toColor()
    private val trailingIconColor = properties.property("trailingIconColor")?.toColor()
    private val underLineColor = properties.property("underLineColor")?.toColor()
    private val capitalization = properties.property("capitalization")?.let {
        when (it) {
            "None" -> KeyboardCapitalization.None
            "Characters" -> KeyboardCapitalization.Characters
            "Words" -> KeyboardCapitalization.Words
            "Sentences" -> KeyboardCapitalization.Sentences
            else -> error("Unknown value for capitalization $it")
        }
    }
    private val keyboardType = properties.property("keyboardType")?.let {
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
    private val imeAction = properties.property("imeAction")?.let {
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
                viewModel.loadComponent(node = serverDrivenNode)?.Content()
            }
        }
    }
    private val placeholder: @Composable (() -> Unit)? = node.propertyNodes("placeholder").let {
        if (it.isEmpty()) return@let null
        return@let {
            for (serverDrivenNode in it) {
                viewModel.loadComponent(node = serverDrivenNode)?.Content()
            }
        }
    }
    private val leadingIcon: @Composable (() -> Unit)? = node.propertyNodes("leadingIcon").let {
        if (it.isEmpty()) return@let null
        return@let {
            for (serverDrivenNode in it) {
                viewModel.loadComponent(node = serverDrivenNode)?.Content()
            }

        }
    }
    private val trailingIcon: @Composable (() -> Unit)? = node.propertyNodes("trailingIcon").let {
        if (it.isEmpty()) return@let null
        return@let {
            for (serverDrivenNode in it) {
                viewModel.loadComponent(node = serverDrivenNode)?.Content()
            }
        }
    }
    private val visualMask = properties.property("visualMask")
    private val visualTransformation = visualMask?.let {
        MaskVisualTransformation(it)
    } ?: VisualTransformation.None
    private val maxLength = properties.property("maxLength")?.toInt() ?: visualMask?.count { it == '#' }
    private val isPassword =
        keyboardType == KeyboardType.Password || keyboardType == KeyboardType.NumberPassword

    @Composable
    override fun Content() {
        val textValue by text.collectAsState()
        val invoker = rememberActionInvoker(viewModel)
        val isEnabled by enabled.collectAsState()
        val isError by isError.collectAsState()
        var passwordVisibility by remember { mutableStateOf(false) }

        TextField(
            value = (textValue).take(maxLength ?: Int.MAX_VALUE),
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) {
                    viewModel.updateState(onChangeUpdateState, it)
                }
            },
            modifier = modifier.observeNodeBounds(node),
            enabled = isEnabled && !invoker.isRunning,
            readOnly = readOnly,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword) {
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
            } else trailingIcon,
            isError = isError,
            visualTransformation = if (isPassword && !passwordVisibility) {
                PasswordVisualTransformation()
            } else visualTransformation,
            keyboardOptions = KeyboardOptions(
                capitalization = capitalization ?: KeyboardCapitalization.None,
                autoCorrectEnabled = autoCorrect ?: true,
                keyboardType = keyboardType ?: KeyboardType.Text,
                imeAction = imeAction ?: ImeAction.Default
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    invoker.invoke(onDone)
                },
                onGo = {
                    invoker.invoke(onGo)
                },
                onNext = {
                    invoker.invoke(onNext)
                },
                onPrevious = {
                    invoker.invoke(onPrevious)
                },
                onSearch = {
                    invoker.invoke(onSearch)
                },
                onSend = {
                    invoker.invoke(onSend)
                },
            ),
            singleLine = singleLine ?: false,
            maxLines = maxLines ?: Int.MAX_VALUE,
            minLines = minLines ?: 1,
            textStyle = TextStyle(
                fontSize = fontSize,
                color = color?.toColor() ?: Color.Unspecified,
            ),
//            prefix = ,
//            suffix =,
//            supportingText = ,
//            interactionSource = ,
//            shape =,
//            colors =,
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