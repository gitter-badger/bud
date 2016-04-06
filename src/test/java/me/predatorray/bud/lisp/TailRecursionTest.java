package me.predatorray.bud.lisp;

import me.predatorray.bud.lisp.evaluator.TocEvaluator;
import me.predatorray.bud.lisp.lang.BudNumber;
import me.predatorray.bud.lisp.test.AbstractInterpreterTest;
import org.junit.Test;

import java.math.BigDecimal;

public class TailRecursionTest extends AbstractInterpreterTest {

    public TailRecursionTest() {
        super(new BudInterpreter(new TocEvaluator()));
    }

    @Test
    public void testTailRecursion1() throws Exception {
        assertInterpretCorrectly(new BudNumber(new BigDecimal(50005000)), "accumulate-1-10000.bud");
    }
}
