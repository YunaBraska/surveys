package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.ContextExchange;

import java.util.Optional;

public class QuestionLong extends FlowItem<Long, QuestionLong> {


    @Override
    public Optional<Long> parse(final ContextExchange exchange) {
        if (exchange.payload() instanceof Number number) {
            return Optional.of(number.longValue());
        }
        try {
            return Optional.of(Long.valueOf(String.valueOf(exchange.payload())));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static QuestionLong of(final Enum<?> label) {
        return label == null ? null : of(label.name());
    }

    public static QuestionLong of(final String label) {
        return new QuestionLong(label);
    }

    private QuestionLong(final String label) {
        super(label);
    }
}
