package berlin.yuna.survey.model.exception;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class FlowImportException extends FlowRuntimeException {

    public FlowImportException(final String flow, final String label, final String message) {
        super(label, flow, message);
    }

    public FlowImportException(final String flow, final String label, final String message, final Throwable throwable) {
        super(label, flow, message, throwable);
    }

}
