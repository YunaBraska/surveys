package berlin.yuna.survey.model.exception;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class FlowRuntimeException extends RuntimeException {

    final String label;
    final String flow;

    public FlowRuntimeException(final String label, final String flow, final String message) {
        this(label, flow, message, null);
    }

    public FlowRuntimeException(final String label, final String flow, final String message, final Throwable throwable) {
        super(message, throwable);
        this.label = label;
        this.flow = flow;
    }

    public String getLabel() {
        return label;
    }

    public String getFlow() {
        return flow;
    }
}
