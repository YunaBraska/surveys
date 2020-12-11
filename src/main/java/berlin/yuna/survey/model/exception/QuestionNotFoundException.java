package berlin.yuna.survey.model.exception;

public class QuestionNotFoundException extends RuntimeException {

    final String label;
    final String flow;

    public static QuestionNotFoundException itemNotFoundInHistory(final String label, final String flow) {
        return new QuestionNotFoundException(label, flow, "History item [" + label + "] was not found in flow [" + flow + "]");
    }

    public static QuestionNotFoundException itemNotFound(final String label, final String flow, final String message) {
        return new QuestionNotFoundException(label, flow, message == null ? "Flow item [" + label + "] was not found" : message);
    }

    public static QuestionNotFoundException itemNotFound(final String label, final String flow) {
        return itemNotFound(label, flow, null);
    }

    public QuestionNotFoundException(final String label, final String flow, final String message) {
        super(message);
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
