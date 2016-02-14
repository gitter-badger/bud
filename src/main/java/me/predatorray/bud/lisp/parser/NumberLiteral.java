package me.predatorray.bud.lisp.parser;

import me.predatorray.bud.lisp.lang.BudNumber;
import me.predatorray.bud.lisp.lang.BudObject;
import me.predatorray.bud.lisp.lang.Environment;
import me.predatorray.bud.lisp.lexer.NumberToken;

import java.math.BigDecimal;

public class NumberLiteral extends TokenLocatedExpression {

    private final BigDecimal value;

    public NumberLiteral(NumberToken numberToken) {
        super(numberToken);
        this.value = numberToken.getValue();
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public BudObject evaluate(Environment environment) {
        return new BudNumber(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NumberLiteral that = (NumberLiteral) o;

        return value.equals(that.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public BigDecimal getValue() {
        return value;
    }
}
