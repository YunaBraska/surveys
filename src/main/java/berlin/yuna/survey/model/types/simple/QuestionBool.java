package berlin.yuna.survey.model.types.simple;

import berlin.yuna.survey.model.types.QuestionGeneric;

import java.util.Optional;

public class QuestionBool extends QuestionGeneric<Boolean, QuestionBool> {

    @Override
    public Optional<Boolean> parse(final Object answer) {
        if (answer instanceof Boolean) {
            return Optional.of((Boolean) answer);
        } else if (answer instanceof Number && ((Number) answer).intValue() == 1) {
            return Optional.of(true);
        } else if (answer instanceof Number && ((Number) answer).intValue() == 0) {
            return Optional.of(false);
        }

        final String s = String.valueOf(answer);
        if (equalsIgnoreCase(s, "0", "no", "false", "reject", "disagree", "cancel", "abort", "refuse", "forbid", "fail", "failed", "error")) {
            return Optional.of(false);
        } else if (equalsIgnoreCase(s, "1", "yes", "true", "agree", "ok", "continue", "succeed", "success", "done")) {
            return Optional.of(true);
        } else {
            return Optional.empty();
        }
    }

    public static QuestionBool of(final Enum<?> label) {
        return label == null ? null : of(label.name());
    }

    public static QuestionBool of(final String label) {
        return getOrNew(label, QuestionBool.class, () -> new QuestionBool(label));
    }

    private boolean equalsIgnoreCase(final String text, final String... anyMatch) {
        for (String anotherString : anyMatch) {
            if (anotherString.equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }

    public QuestionBool(String label) {
        super(label);
    }
}
