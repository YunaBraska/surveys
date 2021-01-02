package berlin.yuna.survey.model.types.simple;

import berlin.yuna.survey.model.types.FlowItem;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Optional;

import static berlin.yuna.survey.config.SurveyDefaults.surveyMapper;

public class Question extends FlowItem<String, Question> {

    @Override
    public Optional<String> parse(final Object answer) {
        return Optional.of(String.valueOf(answer));
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
