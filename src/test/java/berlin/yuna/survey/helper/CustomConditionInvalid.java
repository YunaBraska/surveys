package berlin.yuna.survey.helper;

import berlin.yuna.survey.model.Condition;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class CustomConditionInvalid extends Condition<String> {

    private CustomConditionInvalid() {
        super("");
    }

    @Override
    public boolean apply(final String answer) {
        return answer.equals("4");
    }
}
