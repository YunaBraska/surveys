package berlin.yuna.survey.model.types.simple;

import berlin.yuna.survey.model.types.QuestionGeneric;

import java.util.Optional;

public class QuestionInt extends QuestionGeneric<Integer, QuestionInt> {


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

    public static QuestionInt of(final Enum<?> label) {
        return label == null ? null : of(label.name());
    }

    public static QuestionInt of(final String label) {
        return getOrNew(label, QuestionInt.class, () -> new QuestionInt(label));
    }

    private QuestionInt(String label) {
        super(label);
    }
}
