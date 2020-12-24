package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.Condition;

public class CustomConditionInvalid extends Condition<String> {

    private CustomConditionInvalid() {
        super("");
    }

    @Override
    public boolean apply(final String answer) {
        return answer.equals("4");
    }
}
