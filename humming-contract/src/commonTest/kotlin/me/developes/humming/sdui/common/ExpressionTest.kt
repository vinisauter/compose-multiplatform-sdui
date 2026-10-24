package me.developes.humming.sdui.common

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*
import kotlin.coroutines.cancellation.CancellationException

class ExpressionTest {
    private fun spec(source: String) = ExpressionSpec("humming.expression",1,source)
    private fun eval(source: String, context: ExpressionContext = ExpressionContext(), limits: ExpressionLimits = ExpressionLimits()) =
        compileExpression(spec(source),limits).evaluate(context)
    private fun number(source: String) = (eval(source) as SduiValue.NumberValue).source.toDouble()
    private fun bool(source: String) = (eval(source) as SduiValue.BooleanValue).value
    private fun objectValue(source: String) = parseSduiValue(source) as SduiValue.ObjectValue
    private fun failure(code: String, block: () -> Unit): NodeDiagnostic {
        val diagnostic=assertFailsWith<PayloadException>(block=block).diagnostic
        assertEquals(code,diagnostic.code)
        assertTrue(diagnostic.path.startsWith("$.expression.source["))
        return diagnostic
    }

    @Test fun precedenceAssociativityGroupingUnaryAndRemainderFollowTheSpecifiedGrammar() {
        assertEquals(7.0,number("1 + 2 * 3"))
        assertEquals(9.0,number("(1 + 2) * 3"))
        assertEquals(5.0,number("10 - 3 - 2"))
        assertEquals(2.0,number("20 / 5 / 2"))
        assertEquals(-5.0,number("-2 * 3 + 1"))
        assertEquals(1.0,number("7 % 3"))
        assertTrue(bool("1 + 2 * 3 == 7 && !false || false"))
        assertTrue(bool("!false == true"))
        assertEquals(SduiValue.Text("a\nb"),eval("\"a\\nb\""))
    }

    @Test fun readsOnlyDeclaredScopesWithDynamicKeysAndIndexes() {
        val context=ExpressionContext(
            state=objectValue("""{"key":"name","rows":[{"name":"Ana"},{"name":"Bia"}],"numericId":"0007"}"""),
            server=objectValue("""{"count":2}"""),nav=objectValue("""{"index":1}"""),
            response=objectValue("""{"ok":true}"""),error=objectValue("""{"code":"RETRY"}"""))
        assertEquals(SduiValue.Text("Bia"),eval("state.rows[nav.index][state.key]",context))
        assertEquals(SduiValue.Text("0007"),eval("state.numericId",context))
        assertEquals(SduiValue.BooleanValue(true),eval("response.ok && server.count == 2",context))
        assertEquals(SduiValue.Text("RETRY"),eval("error.code",context))
    }

    @Test fun missingNullAndFalseAreDistinctAndCoalesceIsLazy() {
        assertEquals(SduiValue.Missing,eval("state.absent.child[0]"))
        assertEquals(SduiValue.Missing,eval("null.child"))
        assertEquals(SduiValue.Text("fallback"),eval("state.absent ?? null ?? \"fallback\""))
        assertEquals(SduiValue.BooleanValue(false),eval("false ?? true"))
        assertTrue(bool("missing != null && null == null && missing == missing"))
        assertEquals(SduiValue.NumberValue("5"),eval("5 ?? (1 / 0)"))
        assertEquals(SduiValue.Missing,eval("missing[1 / 0]"))
    }

    @Test fun logicalShortCircuitDoesNotEvaluateErrorsOnUnusedBranches() {
        assertFalse(bool("false && (1 / 0 > 0)"))
        assertTrue(bool("true || (1 / 0 > 0)"))
        failure("EXPRESSION_DIVISION_ZERO") { eval("true && (1 / 0 > 0)") }
        failure("EXPRESSION_TYPE") { eval("1 && true") }
        failure("EXPRESSION_TYPE") { eval("!null") }
    }

    @Test fun operatorsAreStrictAndDoNotCoerceStringsOrContainers() {
        assertEquals(SduiValue.Text("ab"),eval("\"a\" + \"b\""))
        assertFalse(bool("1 == \"1\""))
        assertTrue(bool("1 == 1.0"))
        failure("EXPRESSION_TYPE") { eval("\"1\" + 2") }
        failure("EXPRESSION_TYPE") { eval("\"a\" < \"b\"") }
        failure("EXPRESSION_TYPE") { eval("state == server") }
        failure("EXPRESSION_TYPE") { eval("state[0]") }
        failure("EXPRESSION_TYPE") { eval("(1).field") }
    }

    @Test fun arrayBoundsAndInvalidIndexesHaveExplicitSemantics() {
        val context=ExpressionContext(state=objectValue("""{"rows":[null,"x"]}"""))
        assertEquals(SduiValue.Null,eval("state.rows[0]",context))
        assertEquals(SduiValue.Missing,eval("state.rows[99]",context))
        failure("EXPRESSION_INDEX") { eval("state.rows[-1]",context) }
        failure("EXPRESSION_INDEX") { eval("state.rows[0.5]",context) }
        failure("EXPRESSION_TYPE") { eval("state.rows[\"0\"]",context) }
    }

    @Test fun numericDomainRejectsNonFiniteResultsZeroDivisionAndUnsafeIntegers() {
        failure("EXPRESSION_DIVISION_ZERO") { eval("5 / -0") }
        failure("EXPRESSION_DIVISION_ZERO") { eval("5 % 0") }
        failure("EXPRESSION_NUMBER") { eval("1e999") }
        failure("EXPRESSION_NUMBER") { eval("1 / 1e-309") }
        failure("EXPRESSION_PRECISION") { eval("9007199254740993 == 9007199254740992") }
        failure("EXPRESSION_PRECISION") { eval("9007199254740991 + 1") }
        val context=ExpressionContext(state=objectValue("""{"large":9007199254740993}"""))
        assertEquals(SduiValue.NumberValue("9007199254740993"),eval("state.large",context))
    }

    @Test fun rejectsCallsAssignmentsUnknownScopesInvalidLiteralsAndWrongVersions() {
        for (source in listOf("state.run()","state.x = 1","[1]","{\"x\":1}","1 +", "\"unfinished", "true & false"))
            failure("EXPRESSION_SYNTAX") { eval(source) }
        failure("EXPRESSION_SCOPE") { eval("System.exit") }
        failure("EXPRESSION_SCOPE") { eval("undefined") }
        failure("EXPRESSION_VERSION") { compileExpression(spec("1").copy(version=2)) }
        failure("EXPRESSION_VERSION") { compileExpression(spec("1").copy(language="javascript")) }
        val encoded=Json.encodeToString(spec("state.x ?? 0"))
        assertEquals(spec("state.x ?? 0"),Json.decodeFromString<ExpressionSpec>(encoded))
        assertFails { Json.decodeFromString<ExpressionSpec>("""{"source":"1"}""") }
    }

    @Test fun parserSourceTokensAndBothFormsOfDepthAreBounded() {
        failure("EXPRESSION_LIMIT") { eval("1".repeat(20),limits=ExpressionLimits(maxSourceChars=10)) }
        failure("EXPRESSION_LIMIT") { eval("1 + 2 + 3",limits=ExpressionLimits(maxTokens=3)) }
        failure("EXPRESSION_LIMIT") { eval("1+2+3+4+5",limits=ExpressionLimits(maxDepth=4)) }
        failure("EXPRESSION_LIMIT") { eval("(".repeat(20)+"true"+")".repeat(20),limits=ExpressionLimits(maxDepth=8)) }
        failure("EXPRESSION_LIMIT") { eval("!".repeat(20)+"true",limits=ExpressionLimits(maxDepth=8)) }
    }

    @Test fun evaluationBudgetTextLimitsCancellationAndContextReuseAreIndependent() {
        failure("EXPRESSION_LIMIT") { eval("1 + 2",limits=ExpressionLimits(maxOperations=2)) }
        failure("EXPRESSION_TEXT_LIMIT") { eval("\"ab\" + \"c\"",limits=ExpressionLimits(maxTextChars=2)) }
        failure("EXPRESSION_TEXT_LIMIT") { eval("state.text",ExpressionContext(state=objectValue("""{"text":"abc"}""")),ExpressionLimits(maxTextChars=2)) }
        val program=compileExpression(spec("state.x + 1"))
        assertEquals(3.0,(program.evaluate(ExpressionContext(state=objectValue("""{"x":2}"""))) as SduiValue.NumberValue).source.toDouble())
        assertEquals(8.0,(program.evaluate(ExpressionContext(state=objectValue("""{"x":7}"""))) as SduiValue.NumberValue).source.toDouble())
        val cancellation=CancellationException("cancelled by caller")
        assertSame(cancellation,assertFailsWith<CancellationException> { program.evaluate(ExpressionContext()) { throw cancellation } })
        val diagnostic=failure("EXPRESSION_DIVISION_ZERO") { eval("1 / 0") }
        assertEquals("evaluateExpression",diagnostic.operation)
        assertEquals("$.expression.source[2]",diagnostic.path)
    }
}
