package berlin.yuna.survey.model;

import berlin.yuna.survey.logic.DiagramExporter;

/**
 * {@link Condition} is used for back and forward transitions/{@link Route} in a {@link berlin.yuna.survey.model.types.FlowItem}
 *
 * @param <T> answer type should be the same as the {@link berlin.yuna.survey.model.types.FlowItem} type
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class Condition<T> {

    /**
     * Optional {@code label}
     * Used by rendering diagrams {@link DiagramExporter}
     */
    private final String label;

    /**
     * Constructor without label
     */
    public Condition() {
        this(null);
    }

    /**
     * Constructor with label
     *
     * @param label (optional) used for render diagrams {@link DiagramExporter}
     */
    public Condition(final String label) {
        this.label = label;
    }

    /**
     * Gets the defined label for this {@link Condition}
     *
     * @return defined label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Specifies what happens on on the transition with the given answer
     *
     * @param answer passed for optional usage
     * @return {@code true} if transition is allowed else {@code false}
     */
    public abstract boolean apply(final T answer);
}
