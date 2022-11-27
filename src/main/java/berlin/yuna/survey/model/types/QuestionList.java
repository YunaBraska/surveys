package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.ContextExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static berlin.yuna.survey.config.SurveyDefaults.surveyMapper;
import static java.util.Arrays.stream;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class QuestionList extends FlowItem<Collection<String>, QuestionList> {


    @Override
    public Optional<Collection<String>> parse(final ContextExchange exchange) {
        return collectionOf(exchange.payload(), String.class);
    }

    //TODO: CollectionUtils
    private <T> Optional<Collection<T>> collectionOf(final Object object, final Class<T> type) {
        try {
            if (object != null && object.getClass().isArray()) {
                return Optional.of(stream(((Object[]) object)).map(type::cast).toList());
            } else if (object instanceof Collection) {
                return Optional.of(((Collection<?>) object).stream().map(type::cast).toList());
            } else if (object instanceof String str) {
                return collectionOf(str, type);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private <T> Optional<Collection<T>> collectionOf(final String object, final Class<T> type) {
        final ObjectMapper mapper = surveyMapper();
        return mapper.convertValue(object, mapper.getTypeFactory().constructCollectionType(List.class, type));
    }

    public static QuestionList of(final Enum<?> label) {
        return label == null ? null : of(label.name());
    }

    public static QuestionList of(final String label) {
        return new QuestionList(label);
    }

    private QuestionList(final String label) {
        super(label);
    }
}
