/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Wenhao Ji
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.predatorray.bud.lisp.parser;

import me.predatorray.bud.lisp.evaluator.EvaluatingException;
import me.predatorray.bud.lisp.evaluator.Evaluator;
import me.predatorray.bud.lisp.lang.*;
import me.predatorray.bud.lisp.lexer.LeftParenthesis;
import me.predatorray.bud.lisp.util.Validation;

import java.util.Collections;
import java.util.List;

public class ConditionSpecialForm extends CompoundExpression {

    private final List<ConditionClause> clauses;
    private final Expression elseExpression;

    public ConditionSpecialForm(List<ConditionClause> clauses, Expression elseExpression, LeftParenthesis leading) {
        super(leading, "cond", clauses);
        this.clauses = Validation.notEmpty(clauses);
        this.elseExpression = elseExpression;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public Continuous evaluate(Environment environment, Evaluator evaluator) {
        for (ConditionClause clause : clauses) {
            Expression test = clause.getTest();
            BudObject tested = evaluator.evaluate(test, environment);
            if (BudBoolean.isFalse(tested)) {
                continue;
            }

            if (clause.hasRecipient()) {
                Expression recipient = clause.getRecipient();
                BudObject recipientObj = evaluator.evaluate(recipient, environment);
                if (!BudType.Category.FUNCTION.equals(recipientObj.getType().getCategory())) {
                    throw new NotApplicableException(recipient);
                }
                Function recipientFunction = (Function) recipientObj;
                recipientFunction.inspect(Collections.singletonList(tested.getType()));
                return new TailApplication(recipientFunction, Collections.singletonList(tested));
            } else {
                Expression consequent = clause.getConsequent();
                return new TailExpression(consequent, environment, evaluator);
            }
        }
        if (elseExpression == null) {
            throw new EvaluatingException("all clauses in cond are evaluated to false values and " +
                    "no else-clause is found",
                    this);
        }
        return new TailExpression(elseExpression, environment, evaluator);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConditionSpecialForm that = (ConditionSpecialForm) o;

        if (!clauses.equals(that.clauses)) return false;
        return elseExpression != null ? elseExpression.equals(that.elseExpression) : that.elseExpression == null;
    }

    @Override
    public int hashCode() {
        int result = clauses.hashCode();
        result = 31 * result + (elseExpression != null ? elseExpression.hashCode() : 0);
        return result;
    }

    public List<ConditionClause> getClauses() {
        return clauses;
    }

    public Expression getElseExpression() {
        return elseExpression;
    }
}
