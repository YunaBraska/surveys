package berlin.yuna.survey.helper;

import berlin.yuna.survey.model.Condition;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CustomCondition4 extends Condition<String> {

    public CustomCondition4() {
        super("");
    }

    @Override
    public boolean apply(final String answer) {
        return answer.equals("4");
    }
}
