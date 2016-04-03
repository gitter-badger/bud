package me.predatorray.bud.lisp.buitin.arithmetic;

import me.predatorray.bud.lisp.buitin.NamedFunction;
import me.predatorray.bud.lisp.evaluator.ArgumentTypeMismatchException;
import me.predatorray.bud.lisp.lang.BudNumber;
import me.predatorray.bud.lisp.lang.BudObject;
import me.predatorray.bud.lisp.lang.BudType;

import java.math.BigDecimal;
import java.util.List;

public class AddFunction extends NamedFunction {

    public AddFunction() {
        super("+");
    }

    @Override
    public BudType inspect(List<BudType> argumentTypes) {
        for (int i = 0; i < argumentTypes.size(); i++) {
            BudType argumentType = argumentTypes.get(i);
            if (!BudType.NUMBER.equals(argumentType)) {
                throw new ArgumentTypeMismatchException("expected arguments of type number, but the " +
                        (i + i) + "th argument is " + argumentType);
            }
        }
        return BudType.NUMBER;
    }

    @Override
    public BudObject apply(List<BudObject> arguments) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BudObject argument : arguments) {
            BudNumber number = (BudNumber) argument;
            sum = sum.add(number.getValue());
        }
        return new BudNumber(sum);
    }
}
