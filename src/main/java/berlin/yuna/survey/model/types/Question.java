package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.ContextExchange;

import java.util.Optional;

public class Question extends FlowItem<String, Question> {

    @Override
    public Optional<String> parse(final ContextExchange exchange) {
        return exchange.payload(String.class);
    }

    public static Question of(final Enum<?> label) {
        return label == null ? null : of(label.name());
    }

    public static Question of(final String label) {
        return new Question(label);
    }

    public Question(String label) {
        super(label);
    }
}
