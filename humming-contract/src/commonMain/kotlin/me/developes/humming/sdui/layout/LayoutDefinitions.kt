package me.developes.humming.sdui.layout

import me.developes.humming.sdui.common.InvalidPropertyPolicy
import me.developes.humming.sdui.common.IntegerPropertyOrder
import me.developes.humming.sdui.common.NodeDefinition
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.PropertyDefinition
import me.developes.humming.sdui.common.PropertyKind

/** Editable subset; the remaining renderers still use legacy dispatch. */
object LayoutDefinitions {
    private fun text(name: String, required: Boolean = false, binding: Boolean = false, description: String? = null) =
        PropertyDefinition(name, required = required, allowsBinding = binding, defaultDescription = description)
    private fun number(name: String, kind: PropertyKind = PropertyKind.FLOAT, default: String? = null,
                       fallback: Boolean = false, minimum: Double? = null) =
        PropertyDefinition(name, kind, defaultValue = default, minimum = minimum,
            invalidPolicy = if (fallback) InvalidPropertyPolicy.LEGACY_FALLBACK else InvalidPropertyPolicy.REJECT)
    private fun choice(name: String, options: String, default: String? = null, fallback: Boolean = false) =
        PropertyDefinition(name, PropertyKind.ENUM, defaultValue = default, enumValues = options.split('|').toSet(),
            invalidPolicy = if (fallback) InvalidPropertyPolicy.LEGACY_FALLBACK else InvalidPropertyPolicy.REJECT)
    private fun bool(name: String, default: String, binding: Boolean = false, strict: Boolean = false) =
        PropertyDefinition(name, PropertyKind.BOOLEAN, defaultValue = default, allowsBinding = binding,
            invalidPolicy = if (strict) InvalidPropertyPolicy.LEGACY_FALLBACK else InvalidPropertyPolicy.LEGACY_BOOLEAN)
    private fun nodes(name: String, role: NodeRole) = PropertyDefinition(name, PropertyKind.NODES, nodeRole = role)
    private fun color(name: String) = PropertyDefinition(name, PropertyKind.COLOR)

    val modifiers: List<PropertyDefinition> =
        listOf("width", "height", "minWidth", "minHeight", "paddingAll", "paddingHorizontal",
            "paddingVertical", "paddingStart", "paddingEnd", "paddingTop", "paddingBottom").map { number(it) } +
        listOf("fillMaxSize", "fillMaxWidth", "fillMaxHeight").map {
            number(it, fallback = true).copy(defaultDescription = "absent: no modifier; invalid supplied value: fraction 1")
        } + listOf(
            color("backgroundColor"),
            choice("backgroundShape", "CircleShape|RectangleShape|RoundedCornerShape|CutCornerShape", "RectangleShape", true),
            PropertyDefinition("backgroundGradient", PropertyKind.OPAQUE, editable = false,
                defaultDescription = "Legacy nested brush; preserved, not editable by this schema"),
        ) + listOf("backgroundShapeSize", "backgroundShapeTopStart", "backgroundShapeTopEnd",
            "backgroundShapeBottomStart", "backgroundShapeBottomEnd").map { number(it, PropertyKind.INTEGER, fallback = true) } +
        listOf("verticalScroll", "horizontalScroll").map {
            PropertyDefinition(it, invalidPolicy = InvalidPropertyPolicy.PRESENCE,
                defaultDescription = "absent: disabled; any supplied non-null value enables scrolling")
        }

    private fun component(name: String, properties: List<PropertyDefinition>, children: Boolean = false) =
        NodeDefinition("layout:$name", NodeRole.COMPONENT, modifiers + properties,
            childrenRole = if (children) NodeRole.COMPONENT else null)

    val text = component("text", listOf(
        text("text", required = true, binding = true), number("fontSize", default = "16", fallback = true), color("color"),
        choice("textAlign", "Start|Center|End|Justify", "Start", true),
        choice("fontWeight", "Bold|Thin|ExtraBold").copy(defaultDescription = "absent: FontWeight.Normal"),
    ))
    val button = component("button", listOf(bool("enabled", "true", binding = true, strict = true),
        nodes("onClick", NodeRole.ACTION)), children = true)
    val row = component("row", listOf(
        choice("horizontalArrangement", "Start|Center|End|SpaceAround|SpaceBetween|SpaceEvenly", "Start", true),
        choice("verticalAlignment", "Top|Center|Bottom", "Top", true),
    ), children = true)
    val column = component("column", listOf(
        choice("verticalArrangement", "Top|Center|Bottom|SpaceAround|SpaceBetween|SpaceEvenly", "Top"),
        choice("horizontalAlignment", "Start|Center|End", "Start"), nodes("onClick", NodeRole.ACTION),
    ), children = true)
    val box = component("box", listOf(
        choice("contentAlignment", "TopStart|TopCenter|TopEnd|CenterStart|Center|CenterEnd|BottomStart|BottomCenter|BottomEnd", "TopStart"),
        bool("propagateMinConstraints", "false"),
    ), children = true)
    val textField = component("textField", listOf(
        text("onChangeUpdateState", description = "absent: TextField_<node.id>"), text("text", binding = true),
        bool("enabled", "true", binding = true, strict = true), bool("isError", "false", binding = true),
        bool("readOnly", "false"), bool("autoCorrect", "true"), bool("singleLine", "false"),
        number("fontSize", default = "16", fallback = true), color("color"),
        number("maxLines", PropertyKind.INTEGER, minimum = 1.0).copy(defaultDescription = "absent: 1 when singleLine, otherwise Int.MAX_VALUE"),
        number("minLines", PropertyKind.INTEGER, "1", minimum = 1.0),
        number("maxLength", PropertyKind.INTEGER, minimum = 0.0).copy(defaultDescription = "absent: count of # in visualMask; otherwise unlimited"),
        color("textFieldColor").copy(editable = false, defaultDescription = "Parsed but not applied by the legacy renderer"),
        color("trailingIconColor").copy(editable = false, defaultDescription = "Parsed but not applied by the legacy renderer"),
        color("underLineColor").copy(editable = false, defaultDescription = "Parsed but not applied by the legacy renderer"), text("visualMask"),
        choice("capitalization", "None|Characters|Words|Sentences", "None"),
        choice("keyboardType", "Text|Ascii|Email|Number|NumberPassword|Decimal|Password|Phone|Uri", "Text"),
        choice("imeAction", "Default|Done|Go|Next|None|Previous|Search|Send", "Default"),
    ) + listOf("onDone", "onGo", "onNext", "onPrevious", "onSearch", "onSend").map { nodes(it, NodeRole.ACTION) } +
        listOf("label", "placeholder", "leadingIcon", "trailingIcon").map { nodes(it, NodeRole.COMPONENT) })
        .copy(propertyOrders = listOf(IntegerPropertyOrder("minLines", "maxLines", 1, Int.MAX_VALUE, "singleLine")))

    val reactiveText = component("reactiveText", listOf(
        PropertyDefinition("value", PropertyKind.EXPRESSION_TEXT, required = true),
        PropertyDefinition("visible", PropertyKind.EXPRESSION_BOOLEAN),
        number("fontSize", default = "16", fallback = true), color("color"),
        PropertyDefinition("colorToken", PropertyKind.COLOR_TOKEN),
    )).copy(version = 2)

    val reactiveButton = component("reactiveButton", listOf(
        PropertyDefinition("enabled", PropertyKind.EXPRESSION_BOOLEAN, required = true),
        nodes("onClick", NodeRole.ACTION),
    ), children = true)

    val validatedTextField = component("validatedTextField", listOf(
        text("stateKey", required = true), text("label"),
        PropertyDefinition("valid", PropertyKind.EXPRESSION_BOOLEAN, required = true),
        PropertyDefinition("enabled", PropertyKind.EXPRESSION_BOOLEAN), text("errorText", required = true),
        PropertyDefinition("maskToken", PropertyKind.MASK_TOKEN),
    )).copy(version = 2)

    val all: List<NodeDefinition> = listOf(text, textField, button, row, column, box, reactiveText, reactiveButton, validatedTextField)
}
