package berlin.yuna.survey.model;

import berlin.yuna.survey.logic.DiagramExporter;

/**
 * Used to
 *
 * @param <T> answer type should match the question type
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class Condition<T> {

    /**
     * Optional {@code label}
     * Used by rendering diagrams {@link DiagramExporter}
     */
    private final String label;

    public Condition() {
        this(null);
    }

    /**
     * @param label (optional) used for render diagrams {@link DiagramExporter}
     */
    public Condition(final String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @param answer passed for optional usage
     * @return {@code true} if transition is allowed else {@code false}
     */
    public abstract boolean apply(T answer);
}
