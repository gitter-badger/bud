package me.predatorray.bud.lisp.evaluator;

import me.predatorray.bud.lisp.buitin.BuiltinsEnvironment;
import me.predatorray.bud.lisp.buitin.EqualPredicate;
import me.predatorray.bud.lisp.lang.BudBoolean;
import me.predatorray.bud.lisp.lang.BudList;
import me.predatorray.bud.lisp.lang.BudNumber;
import me.predatorray.bud.lisp.lang.BudObject;
import me.predatorray.bud.lisp.lang.BudString;
import me.predatorray.bud.lisp.lang.Environment;
import me.predatorray.bud.lisp.lang.Symbol;
import me.predatorray.bud.lisp.parser.AndSpecialForm;
import me.predatorray.bud.lisp.parser.ConditionClause;
import me.predatorray.bud.lisp.parser.ConditionSpecialForm;
import me.predatorray.bud.lisp.parser.Definition;
import me.predatorray.bud.lisp.parser.Expression;
import me.predatorray.bud.lisp.parser.IfSpecialForm;
import me.predatorray.bud.lisp.parser.LambdaExpression;
import me.predatorray.bud.lisp.parser.OrSpecialForm;
import me.predatorray.bud.lisp.parser.QuoteSpecialForm;
import me.predatorray.bud.lisp.parser.Variable;
import me.predatorray.bud.lisp.test.AbstractBudLispTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public class EvaluatorsTest extends AbstractBudLispTest {

    private final Environment EMPTY_ENV = new Environment();

    @Parameterized.Parameter(0)
    public Evaluator sut;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[] {new NaiveEvaluator()},
                new Object[] {new ConcurrentEvaluator()}
        );
    }

    @Test
    public void testEvaluateString() throws Exception {
        // "hello" ==> "hello"
        String strVal = "hello";
        Expression expr = newStringLiteral(strVal);
        BudObject evaluated = sut.evaluate(expr, EMPTY_ENV);
        BudObject expected = new BudString(strVal);
        assertEquals(expected, evaluated);
    }

    @Test
    public void testEvaluateBoolean1() throws Exception {
        // #t ==> true
        Expression expr = newBooleanLiteral(true);
        BudObject evaluated = sut.evaluate(expr, EMPTY_ENV);
        BudObject expected = BudBoolean.valueOf(true);
        assertEquals(expected, evaluated);
    }

    @Test
    public void testEvaluateBoolean2() throws Exception {
        // #f ==> false
        Expression expr = newBooleanLiteral(false);
        BudObject evaluated = sut.evaluate(expr, EMPTY_ENV);
        BudObject expected = BudBoolean.valueOf(false);
        assertEquals(expected, evaluated);
    }

    @Test
    public void testEvaluateFn1() throws Exception {
        String strVal = "same";
        // (eq "same" "same") ==> true
        Expression expr = newProcedureCall(newVariable("eqv?"), newStringLiteral(strVal), newStringLiteral(strVal));

        Environment initial = new Environment(Collections.<Variable, BudObject>singletonMap(
                newVariable("eqv?"), new EqualPredicate()));
        BudObject evaluated = sut.evaluate(expr, initial);
        BudObject expected = BudBoolean.valueOf(true);
        assertEquals(expected, evaluated);
    }

    @Test
    public void testEvaluateQuote1() throws Exception {
        // (quote a)
        Expression quote = new QuoteSpecialForm(newSymbolDatum("a"), LP);
        BudObject evaluated = sut.evaluate(quote, EMPTY_ENV);
        BudObject expected = new Symbol("a");
        assertEquals(expected, evaluated);
    }

    @Test
    public void testEvaluateQuote2() throws Exception {
        // (quote (a "str"))
        Expression quote = new QuoteSpecialForm(newCompoundDatum(
                newSymbolDatum("a"), newStringDatum("str")), LP);
        BudObject evaluated = sut.evaluate(quote, EMPTY_ENV);
        BudObject expected = new BudList(null, Arrays.asList(new Symbol("a"), new BudString("str")));
        assertEquals(expected, evaluated);
    }

    @Test
    public void testEvaluateLambda1() throws Exception {
        // ((lambda (str1 str2) (eq str1 str2)) "one" "another") ==> false
        Expression body = newProcedureCall(newVariable("eqv?"), newVariable("str1"), newVariable("str2"));
        Expression lambda = new LambdaExpression(Arrays.asList(newVariable("str1"), newVariable("str2")),
                Collections.<Definition>emptyList(), body, LP);
        Expression application = newProcedureCall(lambda, newStringLiteral("one"), newStringLiteral("another"));

        Environment initial = new Environment(Collections.<Variable, BudObject>singletonMap(
                newVariable("eqv?"), new EqualPredicate()));
        BudObject evaluated = sut.evaluate(application, initial);
        BudObject expected = BudBoolean.valueOf(false);
        assertEquals(expected, evaluated);
    }

    @Test
    public void testEvaluateIf1() throws Exception {
        // (if #t "1" "2") ==> "1"
        String first = "1";
        String second = "2";
        Expression ifSpecialForm = new IfSpecialForm(
                newBooleanLiteral(true), newStringLiteral(first), newStringLiteral(second), LP);
        BudObject evaluated = sut.evaluate(ifSpecialForm, EMPTY_ENV);
        assertEquals("(if #t \"1\" \"2\") ==> \"1\"", new BudString(first), evaluated);
    }

    @Test
    public void testEvaluateIf2() throws Exception {
        // (if #f "1" "2") ==> "2"
        String first = "1";
        String second = "2";
        Expression ifSpecialForm = new IfSpecialForm(
                newBooleanLiteral(false), newStringLiteral(first), newStringLiteral(second), LP);
        BudObject evaluated = sut.evaluate(ifSpecialForm, EMPTY_ENV);
        assertEquals("(if #f \"1\" \"2\") ==> \"2\"", new BudString(second), evaluated);
    }

    @Test
    public void testEvaluateIf3() throws Exception {
        // (if '() "1" "2")
        String first = "1";
        String second = "2";
        Expression ifSpecialForm = new IfSpecialForm(
                newQuoteSpecialForm(newCompoundDatum()), newStringLiteral(first), newStringLiteral(second), LP);
        BudObject evaluated = sut.evaluate(ifSpecialForm, EMPTY_ENV);
        assertEquals("(if '() \"1\" \"2\")", new BudString(first), evaluated);
    }

    @Test
    public void testEvaluateAnd1() throws Exception {
        // (and #f #t) ==> #f
        Expression falseExpression = spy(newBooleanLiteral(false));
        Expression trueExpression = spy(newBooleanLiteral(true));
        Expression andSpecialForm = new AndSpecialForm(Arrays.asList(falseExpression, trueExpression), LP);

        BudObject evaluated = sut.evaluate(andSpecialForm, EMPTY_ENV);

        assertEquals("(and #f #t) ==> #f", BudBoolean.FALSE, evaluated);
        verify(falseExpression).evaluate(same(EMPTY_ENV), same(sut));
        verify(trueExpression, never()).evaluate(same(EMPTY_ENV), any(Evaluator.class));
    }

    @Test
    public void testEvaluateAnd2() throws Exception {
        // (and '() "false") ==> "false"
        Expression[] tests = new Expression[] {
                spy(newQuoteSpecialForm(newCompoundDatum())),
                spy(newStringLiteral("false"))
        };
        Expression andSpecialForm = new AndSpecialForm(Arrays.asList(tests), LP);

        BudObject evaluated = sut.evaluate(andSpecialForm, EMPTY_ENV);

        assertEquals("(and '() \"false\") ==> \"false\"", new BudString("false"), evaluated);
        verify(tests[0]).evaluate(same(EMPTY_ENV), same(sut));
        verify(tests[1]).evaluate(same(EMPTY_ENV), same(sut));
    }

    @Test
    public void testEvaluateAnd3() throws Exception {
        // (and) ==> #t
        Expression andSpecialForm = new AndSpecialForm(Collections.<Expression>emptyList(), LP);
        BudObject evaluated = sut.evaluate(andSpecialForm, EMPTY_ENV);
        assertEquals("(and) ==> #t", BudBoolean.TRUE, evaluated);
    }

    @Test
    public void testEvaluateOr1() throws Exception {
        // (or #t #f) ==> #t
        Expression trueExpression = spy(newBooleanLiteral(true));
        Expression falseExpression = spy(newBooleanLiteral(false));
        Expression orSpecialForm = new OrSpecialForm(Arrays.asList(trueExpression, falseExpression), LP);

        BudObject evaluated = sut.evaluate(orSpecialForm, EMPTY_ENV);

        assertEquals("(or #t #f) ==> #t", BudBoolean.TRUE, evaluated);
        verify(trueExpression).evaluate(same(EMPTY_ENV), same(sut));
        verify(falseExpression, never()).evaluate(same(EMPTY_ENV), any(Evaluator.class));
    }

    @Test
    public void testEvaluateOr2() throws Exception {
        // (or '() #f) ==> #t
        Expression trueExpression = spy(newBooleanLiteral(true));
        Expression falseExpression = spy(newBooleanLiteral(false));
        Expression orSpecialForm = new OrSpecialForm(Arrays.asList(trueExpression, falseExpression), LP);

        BudObject evaluated = sut.evaluate(orSpecialForm, EMPTY_ENV);

        assertEquals("(or '() #f) ==> #t", BudBoolean.TRUE, evaluated);
        verify(trueExpression).evaluate(same(EMPTY_ENV), same(sut));
        verify(falseExpression, never()).evaluate(same(EMPTY_ENV), any(Evaluator.class));
    }

    @Test
    public void testEvaluateOr3() throws Exception {
        // (or) ==> #f
        Expression orSpecialForm = new OrSpecialForm(Collections.<Expression>emptyList(), LP);
        BudObject evaluated = sut.evaluate(orSpecialForm, EMPTY_ENV);
        assertEquals("(or) ==> #f", BudBoolean.FALSE, evaluated);
    }

    @Test
    public void testEvaluateCond1() throws Exception {
        // (cond (#t "1") (#f "2")) ==> "1"
        Expression[] tests = new Expression[] {
                spy(newBooleanLiteral(true)),
                spy(newBooleanLiteral(false))
        };
        ConditionClause[] conditionClauses = new ConditionClause[] {
                ConditionClause.newConditionClauseOfConsequentExpression(tests[0], newStringLiteral("1")),
                ConditionClause.newConditionClauseOfConsequentExpression(tests[1], newStringLiteral("2"))
        };
        Expression condSpecialForm = new ConditionSpecialForm(Arrays.asList(conditionClauses), null, LP);

        BudObject evaluated = sut.evaluate(condSpecialForm, EMPTY_ENV);

        assertEquals("(cond (#t \"1\") (#f \"2\")) ==> \"1\"", new BudString("1"), evaluated);
        verify(tests[0]).evaluate(same(EMPTY_ENV), same(sut));
        verify(tests[1], never()).evaluate(same(EMPTY_ENV), any(Evaluator.class));
    }

    @Test(expected = EvaluatingException.class)
    public void testEvaluateCond2() throws Exception {
        // (cond (#f "1") (#f "2")) ==> error
        Expression[] tests = new Expression[] {
                newBooleanLiteral(false),
                newBooleanLiteral(false)
        };
        ConditionClause[] conditionClauses = new ConditionClause[] {
                ConditionClause.newConditionClauseOfConsequentExpression(tests[0], newStringLiteral("1")),
                ConditionClause.newConditionClauseOfConsequentExpression(tests[1], newStringLiteral("2"))
        };
        Expression condSpecialForm = new ConditionSpecialForm(Arrays.asList(conditionClauses), null, LP);

        sut.evaluate(condSpecialForm, EMPTY_ENV);
    }

    @Test
    public void testEvaluateCond3() throws Exception {
        // (cond (#f "1") (#f "2") (else "3")) ==> "3"
        Expression[] tests = new Expression[] {
                spy(newBooleanLiteral(false)),
                spy(newBooleanLiteral(false))
        };
        ConditionClause[] conditionClauses = new ConditionClause[] {
                ConditionClause.newConditionClauseOfConsequentExpression(tests[0], newStringLiteral("1")),
                ConditionClause.newConditionClauseOfConsequentExpression(tests[1], newStringLiteral("2")),
        };
        Expression elseExpression = newStringLiteral("3");
        Expression condSpecialForm = new ConditionSpecialForm(Arrays.asList(conditionClauses), elseExpression, LP);

        BudObject evaluated = sut.evaluate(condSpecialForm, EMPTY_ENV);

        assertEquals("(cond (#f \"1\") (#f \"2\") (else \"3\")) ==> \"3\"", new BudString("3"), evaluated);
        verify(tests[0]).evaluate(same(EMPTY_ENV), same(sut));
        verify(tests[1]).evaluate(same(EMPTY_ENV), same(sut));
    }

    @Test
    public void testEvaluateCond4() throws Exception {
        // (cond (1 => -)) ==> -1
        Expression test = newNumberLiteral(1);
        Expression recipient = newVariable("-");
        Expression condSpecialForm = new ConditionSpecialForm(
                Collections.singletonList(ConditionClause.newConditionClauseOfRecipientExpression(test, recipient)),
                null, LP);

        BudObject evaluated = sut.evaluate(condSpecialForm, BuiltinsEnvironment.INSTANCE);

        assertEquals("(cond (1 => -)) ==> -1", new BudNumber(new BigDecimal(-1)), evaluated);
    }
}