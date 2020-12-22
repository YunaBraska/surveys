package berlin.yuna.survey.model.exception;

import berlin.yuna.survey.model.types.QuestionGeneric;

import static java.lang.String.format;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class QuestionTypeException extends FlowRuntimeException {

    public QuestionTypeException(final String flow, final QuestionGeneric<?, ?> original, final QuestionGeneric<?, ?> invalid) {
        super(original.toString(), flow, format(
                "Question [%s] is defined with different type [%s] than requested [%s]",
                original.label(),
                original.getClass().getSimpleName(),
                invalid.getClass().getSimpleName()
        ));
    }

}
