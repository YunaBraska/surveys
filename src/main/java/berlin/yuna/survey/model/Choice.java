package berlin.yuna.survey.model;

public abstract class Choice<T> {
    private final String label;

    public Choice() {
        this(null);
    }

    public Choice(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract boolean apply(T answer);
}
