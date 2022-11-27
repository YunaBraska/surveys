package berlin.yuna.survey.helper;

import berlin.yuna.survey.model.Condition;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CustomCondition2 extends Condition<String> {

    public CustomCondition2() {
        super();
    }

    @Override
    public boolean apply(final String answer) {
        return answer.equals("2");
    }
}
