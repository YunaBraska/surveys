package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.ContextExchange;

import java.util.Optional;

public class QuestionInt extends FlowItem<Integer, QuestionInt> {

    @Override
    public Optional<Integer> parse(final ContextExchange exchange) {
        try {
            if (exchange.payload() instanceof Number) {
                return Optional.of(((Number) exchange.payload()).intValue());
            }
            return Optional.of(Integer.valueOf(String.valueOf(exchange.payload())));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static QuestionInt of(final Enum<?> label) {
        return label == null ? null : of(label.name());
    }

    public static QuestionInt of(final String label) {
        return new QuestionInt(label);
    }

    public QuestionInt(String label) {
        super(label);
    }
}
