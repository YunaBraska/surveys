package berlin.yuna.survey.helper;

import berlin.yuna.survey.model.Condition;

import java.math.BigInteger;

public class CustomCondition3 extends Condition<BigInteger> {

    public CustomCondition3() {
        super("Integer is 3");
    }

    @Override
    public boolean apply(final BigInteger answer) {
        return answer.intValue() == 3;
    }
}
