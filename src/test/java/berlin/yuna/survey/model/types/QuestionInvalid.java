package berlin.yuna.survey.model.types;

import java.util.Optional;

public class QuestionInvalid extends QuestionGeneric<Integer, QuestionInvalid> {

    @Override
    public Optional<Integer> parse(final Object answer) {
        try {
            if (answer instanceof Number) {
                return Optional.of(((Number) answer).intValue());
            }
            return Optional.of(Integer.valueOf(String.valueOf(answer)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static QuestionInvalid of(final Enum<?> label) {
        return label == null ? null : of(label.name());
    }

    public static QuestionInvalid of(final String label) {
        return new QuestionInvalid(label);
    }

    private QuestionInvalid(String label) {
        super(label);
    }
}
