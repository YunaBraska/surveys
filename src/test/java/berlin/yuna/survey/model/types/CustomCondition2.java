package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.Condition;

public class CustomCondition2 extends Condition<String> {

    public CustomCondition2() {
        super();
    }

    @Override
    public boolean apply(final String answer) {
        return answer.equals("2");
    }
}
