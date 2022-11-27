package berlin.yuna.survey.helper;

import berlin.yuna.survey.model.Condition;

@SuppressWarnings("unused")
public class CustomCondition extends Condition<String> {

    public CustomCondition() {
        super("If equals 1");
    }

    @Override
    public boolean apply(final String answer) {
        return answer.equals("1");
    }
}
