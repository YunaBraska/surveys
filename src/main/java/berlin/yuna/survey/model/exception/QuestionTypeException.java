package berlin.yuna.survey.model.exception;

import berlin.yuna.survey.model.types.QuestionGeneric;

import static java.lang.String.format;

public class QuestionTypeException extends RuntimeException {

    final String label;
    final String flow;

    public QuestionTypeException(final String flow, final QuestionGeneric<?,?> original, final QuestionGeneric<?,?> invalid) {
        super(format(
                "Question [%s] is defined with different type [%s] than requested [%s]",
                original.label(),
                original.getClass().getSimpleName(),
                invalid.getClass().getSimpleName()
        ));
        this.label = original.toString();
        this.flow = flow;
    }

    public String getLabel() {
        return label;
    }

    public String getFlow() {
        return flow;
    }
}
