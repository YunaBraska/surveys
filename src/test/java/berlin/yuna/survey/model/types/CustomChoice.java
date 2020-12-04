package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.Choice;

public class CustomChoice extends Choice<String> {

    public CustomChoice() {
        super("If equals 1");
    }

    @Override
    public boolean apply(final String answer) {
        return answer.equals("1");
    }
}
