package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.ContextExchange;

import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class QuestionBool extends FlowItem<Boolean, QuestionBool> {

    @Override
    public Optional<Boolean> parse(final ContextExchange exchange) {
        if (exchange.payload() instanceof Boolean bool) {
            return Optional.of(bool);
        } else if (exchange.payload() instanceof Number number && number.intValue() == 1) {
            return Optional.of(true);
        } else if (exchange.payload() instanceof Number number && number.intValue() == 0) {
            return Optional.of(false);
        }

        final String s = String.valueOf(exchange.payload());
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
        return new QuestionBool(label);
    }

    private boolean equalsIgnoreCase(final String text, final String... anyMatch) {
        for (String anotherString : anyMatch) {
            if (anotherString.equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }

    public QuestionBool(final String label) {
        super(label);
    }
}
