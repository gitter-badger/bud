package me.predatorray.bud.lisp.buitin.arithmetic;

import me.predatorray.bud.lisp.evaluator.ArgumentTypeMismatchException;
import me.predatorray.bud.lisp.lang.BudNumber;
import me.predatorray.bud.lisp.lang.BudObject;
import me.predatorray.bud.lisp.lang.BudType;
import me.predatorray.bud.lisp.lang.Function;
import me.predatorray.bud.lisp.lang.FunctionType;

import java.math.BigDecimal;
import java.util.List;

public class MultiplyFunction implements Function {

    private final FunctionType thisType = new FunctionType(this);

    @Override
    public BudType inspect(List<BudType> argumentTypes) {
        for (int i = 0; i < argumentTypes.size(); i++) {
            BudType argumentType = argumentTypes.get(i);
            if (!BudType.NUMBER.equals(argumentType)) {
                throw new ArgumentTypeMismatchException("expected arguments of type number, but the " +
                        (i + 1) + "th argument is " + argumentType);
            }
        }
        return BudType.NUMBER;
    }

    @Override
    public BudObject apply(List<BudObject> arguments) {
        if (arguments.isEmpty()) {
            return new BudNumber(BigDecimal.ONE);
        }

        BigDecimal result = null;
        for (BudObject argument : arguments) {
            BigDecimal number = ((BudNumber) argument).getValue();
            if (result == null) {
                result = number;
            } else {
                result = result.multiply(number);
            }
        }
        return new BudNumber(result);
    }

    @Override
    public BudType getType() {
        return thisType;
    }
}
