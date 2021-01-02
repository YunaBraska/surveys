package berlin.yuna.survey.model.exception;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class NotJsonPersonableType extends FlowRuntimeException {

    public NotJsonPersonableType(final String label, final String flow, final String message) {
        super(label, flow, message);
    }
}
