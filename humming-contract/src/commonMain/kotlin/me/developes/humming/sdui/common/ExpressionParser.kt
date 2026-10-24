package me.developes.humming.sdui.common

private data class ExpressionToken(val kind: String, val text: String, val offset: Int)

internal class ExpressionParser(private val source: String, private val limits: ExpressionLimits) {
    private var position=0
    private var tokens=0
    private var nesting=0
    private var token=next()
    private fun fail(message: String, offset: Int = token.offset): Nothing =
        expressionFailure("EXPRESSION_SYNTAX",message,offset,"parseExpression")
    private fun limit(message: String, offset: Int): Nothing =
        expressionFailure("EXPRESSION_LIMIT",message,offset,"parseExpression")
    private fun digit(c: Char) = c in '0'..'9'
    private fun letter(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun next(): ExpressionToken {
        while (position < source.length && source[position] in " \t\n\r") position++
        val start=position
        if (position == source.length) return ExpressionToken("end","",position)
        if (++tokens > limits.maxTokens) limit("Token budget exceeded",start)
        val c=source[position++]
        if (letter(c)) {
            while (position < source.length && (letter(source[position]) || digit(source[position]))) position++
            return ExpressionToken("name",source.substring(start,position),start)
        }
        if (digit(c)) {
            while (position < source.length && digit(source[position])) position++
            if (position < source.length && source[position] == '.') {
                position++
                if (position == source.length || !digit(source[position])) fail("Expected fractional digits",position)
                while (position < source.length && digit(source[position])) position++
            }
            if (position < source.length && source[position] in "eE") {
                position++
                if (position < source.length && source[position] in "+-") position++
                if (position == source.length || !digit(source[position])) fail("Expected exponent digits",position)
                while (position < source.length && digit(source[position])) position++
            }
            return ExpressionToken("number",source.substring(start,position),start)
        }
        if (c == '"') {
            while (position < source.length) {
                when (source[position++]) {
                    '\\' -> if (position < source.length) position++ else fail("Unterminated escape",start)
                    '"' -> return ExpressionToken("string",source.substring(start,position),start)
                }
            }
            fail("Unterminated string",start)
        }
        val single=c.toString()
        if (single in listOf("(",")","[","]",".","+","-","*","/","%")) return ExpressionToken(single,single,start)
        if (c in "!<>") {
            val op=if (position < source.length && source[position] == '=') { position++; "$c=" } else single
            return ExpressionToken(op,op,start)
        }
        if (c in "&|?=" && position < source.length && source[position] == c) {
            position++
            return ExpressionToken("$c$c","$c$c",start)
        }
        fail("Unexpected character",start)
    }
    private fun take(): ExpressionToken = token.also { token=next() }
    private fun expect(kind: String) { if (token.kind != kind) fail("Expected $kind"); take() }
    private fun bounded(node: ExpressionNode): ExpressionNode {
        if (node.depth > limits.maxDepth) limit("AST depth budget exceeded",node.offset)
        return node
    }
    fun parse(): ExpressionNode {
        val node=expression(0)
        if (token.kind != "end") fail("Unexpected token")
        return node
    }
    private fun expression(minimum: Int): ExpressionNode {
        if (++nesting > limits.maxDepth) limit("Parser depth budget exceeded",token.offset)
        try {
            val first=take()
            var left: ExpressionNode=bounded(when (first.kind) {
                "number","string" -> {
                    val value=try { parseSduiValue(first.text) } catch (error: PayloadException) {
                        expressionFailure(if (error.diagnostic.code == "VALUE_NUMBER") "EXPRESSION_NUMBER" else "EXPRESSION_SYNTAX",
                            "Invalid literal",first.offset,"parseExpression")
                    }
                    ExpressionNode.Literal(value,first.offset)
                }
                "name" -> when (first.text) {
                    "null" -> ExpressionNode.Literal(SduiValue.Null,first.offset)
                    "missing" -> ExpressionNode.Literal(SduiValue.Missing,first.offset)
                    "true","false" -> ExpressionNode.Literal(SduiValue.BooleanValue(first.text == "true"),first.offset)
                    "state","server","nav","response","error" -> ExpressionNode.Scope(first.text,first.offset)
                    else -> expressionFailure("EXPRESSION_SCOPE","Unknown scope: ${first.text}",first.offset,"parseExpression")
                }
                "!","-" -> ExpressionNode.Unary(first.kind,expression(70),first.offset)
                "(" -> expression(0).also { expect(")") }
                else -> fail("Expected literal, scope or parenthesized expression",first.offset)
            })
            while (true) {
                if (token.kind == "." || token.kind == "[") {
                    if (80 < minimum) break
                    val op=take()
                    val key=if (op.kind == ".") {
                        if (token.kind != "name") fail("Expected member name")
                        val name=take()
                        ExpressionNode.Literal(SduiValue.Text(name.text),name.offset)
                    } else expression(0).also { expect("]") }
                    left=bounded(ExpressionNode.Access(left,key,op.offset))
                    continue
                }
                val precedence=when (token.kind) {
                    "??" -> 5; "||" -> 10; "&&" -> 20; "==","!=" -> 30; "<","<=",">",">=" -> 40
                    "+","-" -> 50; "*","/","%" -> 60; else -> break
                }
                if (precedence < minimum) break
                val op=take()
                left=bounded(ExpressionNode.Binary(op.kind,left,expression(precedence+1),op.offset))
            }
            return left
        } finally { nesting-- }
    }
}
