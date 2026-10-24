package me.developes.humming.sdui.common

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.floor

@Serializable
data class ExpressionSpec(val language: String, val version: Int, val source: String)

data class ExpressionLimits(val maxSourceChars: Int = 4096, val maxTokens: Int = 1024,
                            val maxDepth: Int = 64, val maxOperations: Int = 10_000,
                            val maxTextChars: Int = 65_536) {
    init { require(maxSourceChars > 0 && maxTokens > 0 && maxDepth in 1..128 && maxOperations > 0 && maxTextChars > 0) }
}

data class ExpressionContext(
    val state: SduiValue.ObjectValue = SduiValue.ObjectValue(emptyMap()),
    val server: SduiValue.ObjectValue = SduiValue.ObjectValue(emptyMap()),
    val nav: SduiValue.ObjectValue = SduiValue.ObjectValue(emptyMap()),
    val response: SduiValue = SduiValue.Missing,
    val error: SduiValue = SduiValue.Missing,
)

internal sealed class ExpressionNode(val offset: Int, val depth: Int) {
    class Literal(val value: SduiValue, offset: Int) : ExpressionNode(offset,1)
    class Scope(val name: String, offset: Int) : ExpressionNode(offset,1)
    class Access(val base: ExpressionNode, val key: ExpressionNode, offset: Int) : ExpressionNode(offset,maxOf(base.depth,key.depth)+1)
    class Unary(val op: String, val child: ExpressionNode, offset: Int) : ExpressionNode(offset,child.depth+1)
    class Binary(val op: String, val left: ExpressionNode, val right: ExpressionNode, offset: Int) :
        ExpressionNode(offset,maxOf(left.depth,right.depth)+1)
}

internal fun expressionFailure(code: String, message: String, offset: Int, operation: String): Nothing =
    throw PayloadException(NodeDiagnostic(code,message,operation,"$.expression.source[$offset]"))

class ExpressionProgram internal constructor(private val root: ExpressionNode, private val limits: ExpressionLimits) {
    /** No IO or reflection; the trusted caller may supply cancellation without a coroutines dependency. */
    fun evaluate(context: ExpressionContext, checkCancellation: () -> Unit = {}): SduiValue {
        var operations=0
        fun fail(code: String, message: String, node: ExpressionNode): Nothing =
            expressionFailure(code,message,node.offset,"evaluateExpression")
        fun number(value: SduiValue, node: ExpressionNode): Double {
            if (value !is SduiValue.NumberValue) fail("EXPRESSION_TYPE","Expected Number",node)
            val d=checkNotNull(value.source.toDoubleOrNull())
            if (floor(d) == d && abs(d) > 9007199254740991.0)
                fail("EXPRESSION_PRECISION","Integer exceeds the safe arithmetic range",node)
            return d
        }
        fun boolean(value: SduiValue, node: ExpressionNode): Boolean {
            if (value !is SduiValue.BooleanValue) fail("EXPRESSION_TYPE","Expected Boolean",node)
            return value.value
        }
        fun numericResult(value: Double, node: ExpressionNode): SduiValue {
            if (!value.isFinite()) fail("EXPRESSION_NUMBER","Non-finite arithmetic result",node)
            if (floor(value) == value && abs(value) > 9007199254740991.0)
                fail("EXPRESSION_PRECISION","Integer result exceeds the safe arithmetic range",node)
            return SduiValue.NumberValue(value.toString())
        }
        fun equal(a: SduiValue, b: SduiValue, node: ExpressionNode): Boolean = when {
            a === SduiValue.Missing || b === SduiValue.Missing -> a === b
            a === SduiValue.Null || b === SduiValue.Null -> a === b
            a is SduiValue.NumberValue && b is SduiValue.NumberValue -> number(a,node) == number(b,node)
            a is SduiValue.Text && b is SduiValue.Text -> a.value == b.value
            a is SduiValue.BooleanValue && b is SduiValue.BooleanValue -> a.value == b.value
            a is SduiValue.ObjectValue && b is SduiValue.ObjectValue || a is SduiValue.ArrayValue && b is SduiValue.ArrayValue ->
                fail("EXPRESSION_TYPE","Container equality is not supported",node)
            else -> false
        }
        fun eval(node: ExpressionNode, depth: Int): SduiValue {
            checkCancellation()
            if (++operations > limits.maxOperations || depth > limits.maxDepth)
                fail("EXPRESSION_LIMIT","Evaluation budget exceeded",node)
            val value=when (node) {
                is ExpressionNode.Literal -> node.value
                is ExpressionNode.Scope -> when (node.name) {
                    "state" -> context.state; "server" -> context.server; "nav" -> context.nav
                    "response" -> context.response; "error" -> context.error
                    else -> error("Unknown compiled scope")
                }
                is ExpressionNode.Access -> {
                    val base=eval(node.base,depth+1)
                    if (base === SduiValue.Missing || base === SduiValue.Null) SduiValue.Missing
                    else {
                        val key=eval(node.key,depth+1)
                        when (base) {
                            is SduiValue.ObjectValue -> {
                                if (key !is SduiValue.Text) fail("EXPRESSION_TYPE","Object key must be Text",node)
                                base.values[key.value] ?: SduiValue.Missing
                            }
                            is SduiValue.ArrayValue -> {
                                val index=number(key,node)
                                if (index < 0 || floor(index) != index) fail("EXPRESSION_INDEX","Array index must be a non-negative integer",node)
                                if (index >= base.values.size) SduiValue.Missing else base.values[index.toInt()]
                            }
                            else -> fail("EXPRESSION_TYPE","Cannot access a scalar",node)
                        }
                    }
                }
                is ExpressionNode.Unary -> {
                    val child=eval(node.child,depth+1)
                    when (node.op) {
                        "!" -> SduiValue.BooleanValue(!boolean(child,node))
                        "-" -> numericResult(-number(child,node),node)
                        else -> error("Unknown compiled unary operator")
                    }
                }
                is ExpressionNode.Binary -> {
                    val left=eval(node.left,depth+1)
                    when (node.op) {
                        "&&" -> SduiValue.BooleanValue(boolean(left,node) && boolean(eval(node.right,depth+1),node))
                        "||" -> SduiValue.BooleanValue(boolean(left,node) || boolean(eval(node.right,depth+1),node))
                        "??" -> if (left === SduiValue.Null || left === SduiValue.Missing) eval(node.right,depth+1) else left
                        else -> {
                            val right=eval(node.right,depth+1)
                            when (node.op) {
                                "==" -> SduiValue.BooleanValue(equal(left,right,node))
                                "!=" -> SduiValue.BooleanValue(!equal(left,right,node))
                                "+" -> if (left is SduiValue.Text && right is SduiValue.Text) {
                                    if (left.value.length.toLong()+right.value.length > limits.maxTextChars)
                                        fail("EXPRESSION_TEXT_LIMIT","Text result budget exceeded",node)
                                    SduiValue.Text(left.value+right.value)
                                } else numericResult(number(left,node)+number(right,node),node)
                                "-" -> numericResult(number(left,node)-number(right,node),node)
                                "*" -> numericResult(number(left,node)*number(right,node),node)
                                "/", "%" -> {
                                    val a=number(left,node); val b=number(right,node)
                                    if (b == 0.0) fail("EXPRESSION_DIVISION_ZERO","Division or remainder by zero",node)
                                    numericResult(if (node.op == "/") a/b else a%b,node)
                                }
                                "<" -> SduiValue.BooleanValue(number(left,node)<number(right,node))
                                "<=" -> SduiValue.BooleanValue(number(left,node)<=number(right,node))
                                ">" -> SduiValue.BooleanValue(number(left,node)>number(right,node))
                                ">=" -> SduiValue.BooleanValue(number(left,node)>=number(right,node))
                                else -> error("Unknown compiled binary operator")
                            }
                        }
                    }
                }
            }
            if (value is SduiValue.Text && value.value.length > limits.maxTextChars)
                fail("EXPRESSION_TEXT_LIMIT","Text value budget exceeded",node)
            return value
        }
        return eval(root,1)
    }
}

fun compileExpression(spec: ExpressionSpec, limits: ExpressionLimits = ExpressionLimits()): ExpressionProgram {
    if (spec.language != "humming.expression" || spec.version != 1)
        expressionFailure("EXPRESSION_VERSION","Unsupported expression language/version",0,"parseExpression")
    if (spec.source.length > limits.maxSourceChars)
        expressionFailure("EXPRESSION_LIMIT","Source budget exceeded",0,"parseExpression")
    return ExpressionProgram(ExpressionParser(spec.source,limits).parse(),limits)
}
