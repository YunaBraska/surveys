package berlin.yuna.survey.helper;

import berlin.yuna.survey.model.ContextExchange;
import berlin.yuna.survey.model.types.FlowItem;

import java.util.Optional;

public class QuestionInvalid extends FlowItem<Integer, QuestionInvalid> {

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
