package berlin.yuna.survey.model.types;

import berlin.yuna.survey.logic.DiagramExporter;
import berlin.yuna.survey.logic.Survey;
import berlin.yuna.survey.model.Condition;
import berlin.yuna.survey.model.HistoryItem;
import berlin.yuna.survey.model.Route;
import berlin.yuna.survey.model.exception.FlowRuntimeException;
import berlin.yuna.survey.model.exception.QuestionTypeException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class FlowItem<T, C extends FlowItem<T, C>> implements Comparable<FlowItem<?, ?>> {

    private final String label;
    private final Set<FlowItem<?, ?>> parents = ConcurrentHashMap.newKeySet();
    private final TransitionSet<T> transitions;
    private static final Pattern SPECIAL_CHARS = Pattern.compile("^[A-Z_0-9]*$");

    public FlowItem(final String label) {
        validateNewLabel(label);
        this.label = label;
        this.transitions = new TransitionSet<>(this);
    }

    /**
     * Get a flow item by the given {@code enum}
     *
     * @param label The {@code label} to search in flow
     * @return Returns {@link Optional< FlowItem >} or {@link Optional#empty()} when flow doesn't contain the
     * requested item
     */
    public Optional<FlowItem<?, ?>> get(final Enum<?> label) {
        return label == null ? Optional.empty() : get(label.name());
    }

    /**
     * Get a flow item by the given {@code String}
     *
     * @param label The {@code label} to search in flow
     * @return Returns {@link Optional< FlowItem >} or {@link Optional#empty()} when flow doesn't contain the
     * requested item
     */
    public Optional<FlowItem<?, ?>> get(final String label) {
        return label == null ? Optional.empty() : find(label);
    }

    /**
     * Get a flow item by the given {@link FlowItem}
     *
     * @param type {@link FlowItem} to search in flow
     * @return Returns {@link Optional< FlowItem >} or {@link Optional#empty()} when flow doesn't contain the
     * requested item
     */
    public <I extends FlowItem<?, ?>> Optional<I> get(final I type) {
        return label == null ? Optional.empty() : find(type);
    }

    /**
     * Get a flow item by this {@code String}
     *
     * @param label    The {@code label} to search in flow
     * @param fallback {@link FlowItem} to return if the flow item wasn't found
     * @return Returns {@link FlowItem} or {@code fallback} when flow doesn't contain the
     * requested item
     */
    public FlowItem<?, ?> getOrElse(final String label, final FlowItem<?, ?> fallback) {
        return label == null ? fallback : get(label).orElse(fallback);
    }

    /**
     * Get a flow item by this {@code enum}
     *
     * @param label    The {@code label} to search in flow
     * @param fallback {@link FlowItem} to return if the flow item wasn't found
     * @return Returns {@link FlowItem} or {@code fallback} when flow doesn't contain the
     * requested item
     */
    public FlowItem<?, ?> getOrElse(final Enum<?> label, final FlowItem<?, ?> fallback) {
        return label == null ? fallback : get(label).orElse(fallback);
    }

    /**
     * Get a flow item by this {@code type}
     *
     * @param type     The {@code label} to search in flow
     * @param fallback {@link FlowItem} to return if the flow item wasn't found
     * @return Returns {@link FlowItem} or {@code fallback} when flow doesn't contain the
     * requested item
     */
    public <I extends FlowItem<?, ?>> I getOrElse(final I type, I fallback) {
        return type == null ? fallback : get(type).orElse(fallback);
    }

    /**
     * Defines a transition target of {@link FlowItem#targets()}
     *
     * @param target defines the transition target
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C target(final FlowItem<?, ?> target) {
        targetGet(target);
        return (C) this;
    }

    /**
     * Defines a transition target of {@link FlowItem#targets()}
     *
     * @param target    defines the transition target
     * @param condition {@code condition} which to match. On {@code true} will execute the transition - Conditions
     *                  can be also used to trigger a process
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C target(final FlowItem<?, ?> target, final Function<T, Boolean> condition) {
        targetGet(target, condition);
        return (C) this;
    }

    /**
     * Defines a transition target of {@link FlowItem#targets()}
     *
     * @param target    defines the transition target
     * @param condition {@code condition} which to match. On {@code true} will execute the transition - Conditions
     *                  can be also used to trigger a process
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C target(final FlowItem<?, ?> target, final Condition<T> condition) {
        targetGet(target, condition);
        return (C) this;
    }

    /**
     * Defines a transition target of {@link FlowItem#targets()}
     *
     * @param target    defines the transition target
     * @param condition {@code condition} which to match. On {@code true} will execute the transition - Conditions
     *                  can be also used to trigger a process
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C target(final FlowItem<?, ?> target, final Class<? extends Condition<?>> condition) {
        try {
            targetGet(target, condition == null ? null : (Condition<T>) condition.getConstructor().newInstance());
        } catch (Exception e) {
            throw new FlowRuntimeException(label, null, "Condition construction error", e);
        }
        return (C) this;
    }

    /**
     * Defines a transition target of {@link FlowItem#targets()}
     *
     * @param target defines the transition target
     * @return returns the {@code target} object
     */
    public <I extends FlowItem<?, ?>> I targetGet(final I target) {
        return transitions.pointToAndGet(target, null, null);
    }

    /**
     * Defines a transition target of {@link FlowItem#targets()}
     *
     * @param target    defines the transition target
     * @param condition {@code condition} which to match. On {@code true} will execute the transition - Conditions
     *                  can be also used to trigger a process
     * @return returns the {@code target} object
     */
    public <I extends FlowItem<?, ?>> I targetGet(final I target, final Function<T, Boolean> condition) {
        return transitions.pointToAndGet(target, null, condition);
    }

    /**
     * Defines a transition target of {@link FlowItem#targets()}
     *
     * @param target    defines the transition target
     * @param condition {@code condition} which to match. On {@code true} will execute the transition - Conditions
     *                  can be also used to trigger a process
     * @return returns the {@code target} object
     */
    public <I extends FlowItem<?, ?>> I targetGet(final I target, final Condition<T> condition) {
        return transitions.pointToAndGet(target, condition, null);
    }

    /**
     * Returns a {@link Set} with all configured targets
     *
     * @return a set view of all configured {@code targets} for this {@link FlowItem} object
     */
    public Set<FlowItem<?, ?>> targets() {
        return transitions.forwardTargets();
    }


    /**
     * Defines a back transition of {@link FlowItem#targets()}
     *
     * @param condition {@code condition} which to match. On {@code true} will execute the transition - Conditions
     *                  can be also used to trigger a process
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C onBack(final Function<T, Boolean> condition) {
        transitions.backCondition(null, condition);
        return (C) this;
    }

    /**
     * Defines a back transition of {@link FlowItem#targets()}
     *
     * @param condition {@code condition} which to match. On {@code true} will execute the transition - Conditions
     *                  can be also used to trigger a process
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C onBack(final Condition<T> condition) {
        transitions.backCondition(condition, null);
        return (C) this;
    }

    /**
     * Defines a back transition target of {@link FlowItem#targets()}
     *
     * @param condition {@code condition} which to match. On {@code true} will execute the transition - Conditions
     *                  can be also used to trigger a process
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C onBack(final Class<? extends Condition<?>> condition) {
        try {
            transitions.backCondition(condition == null ? null : (Condition<T>) condition.getConstructor().newInstance(), null);
        } catch (Exception e) {
            throw new FlowRuntimeException(label, null, "Condition construction error", e);
        }
        return (C) this;
    }

    /**
     * Returns a {@link Set} with all configured targets
     *
     * @return a set view of all configured {@code targets} for this {@link FlowItem} object
     */
    public Set<FlowItem<?, ?>> targetsBack() {
        return transitions.backwardTargets();
    }


    /**
     * Returns a {@link Set} with all configured targets
     *
     * @return a set view of all configured {@code targets} for this {@link FlowItem} object
     */
    public Set<FlowItem<?, ?>> parents() {
        return parents;
    }

    /**
     * Answers the question with {@code null} and returns next question
     *
     * @return empty if there is no next question configured
     */
    public Optional<FlowItem<?, ?>> answer() {
        return answer(null);
    }

    /**
     * Converts input for the answer - used to solve {@link FlowItem#parseAndAnswer(Object)}
     *
     * @param answer input parameter to be parsed for the condition of a configured {@link Route}
     * @return depends on the implementation - null usually means its not parsable which needs to be cached at
     */
    public abstract Optional<T> parse(final Object answer);

    /**
     * Returns next {@link FlowItem} which is configured for a back transition
     *
     * @param answer input parameter
     * @return next ({@code backTransition}) {@link FlowItem}
     */
    public Optional<FlowItem<?, ?>> parseAndAnswer(final Object answer) {
        return parse(answer).flatMap(this::answer);
    }

    /**
     * Returns next {@link FlowItem} which is configured for a back transition
     *
     * @param answer input parameter
     * @return returns next ({@code backTransition}) {@link FlowItem}
     */
    public Optional<FlowItem<?, ?>> answer(final T answer) {
        if (answer != null) {
            for (Route<T> route : transitions.forwardRoutes()) {
                if (route.hasCondition() && route.apply(answer)) {
                    return Optional.of(route.target());
                }
            }
        }
        return transitions.forwardRoutes().stream().filter(Route::hasNoCondition).findFirst().map(Route::target);
    }

    /**
     * Reverts configured actions for a back transitions
     *
     * @param answer input parameter
     * @return {@code true} if transition is allowed (empty if no transition is configured)
     */
    public Optional<Boolean> parseAndRevert(final Object answer) {
        return parse(answer).flatMap(this::revert);
    }

    /**
     * Returns next {@link FlowItem} which is configured for a back transition
     *
     * @param answer input parameter
     * @return {@code true} if transition is allowed (empty if no transition is configured)
     */
    public Optional<Boolean> revert(final T answer) {
        if (answer != null) {
            for (Route<T> route : transitions.backwardRoutes()) {
                if (route.hasCondition()) {
                    return Optional.of(route.apply(answer));
                }
            }
        }
        return transitions.backwardRoutes().stream().filter(Route::hasNoCondition).findFirst().map(route -> route.apply(answer));
    }

    /**
     * @return label
     */
    public String label() {
        return label;
    }

    /**
     * @param answer input for check
     * @return {@code true} if {@link HistoryItem} relates to the current {@link FlowItem}
     */
    public boolean match(final HistoryItem answer) {
        return answer != null && label.equals(answer.getLabel());
    }

    /**
     * @return a copy of configured routes
     */
    public TransitionSet<T> transitions() {
        return transitions;
    }

    /**
     * @return a copy of configured routes
     */
    public Set<Route<T>> routes() {
        return new HashSet<>(transitions);
    }

    /**
     * Prepares diagram renderer
     *
     * @return {@link DiagramExporter} renderer
     */
    public DiagramExporter diagram() {
        return new DiagramExporter(Survey.init(this));
    }

    /**
     * Search {@link FlowItem} in current flow
     *
     * @param search type to search in flow
     * @return {@link Optional#empty()} of not found
     */
    @SuppressWarnings("unchecked")
    public <I extends FlowItem<?, ?>> Optional<I> find(final I search) {
        Optional<FlowItem<?, ?>> result = find(search.label());
        assertSameType(result.orElse(null), search);
        return result.isEmpty() ? Optional.empty() : Optional.of((I) result.get());
    }

    private static void validateNewLabel(final String label) {
        if (!SPECIAL_CHARS.matcher(label).find()) {
            throw new IllegalArgumentException("Label should only contain enum able characters like [A-Z_0-9]");
        }
    }

    @SuppressWarnings({"unchecked"})
    protected void addTarget(final Route<?> route) {
        transitions.add((Route<T>) route);
    }

    protected void addParent(final FlowItem<?, ?> parent) {
        parents.add(parent);
    }

    private Optional<FlowItem<?, ?>> find(final String search) {
        return find(this, search, new HashSet<>(), true, true);
    }

    private Optional<FlowItem<?, ?>> find(
            final FlowItem<?, ?> current,
            final String search,
            final HashSet<String> checked,
            final boolean parents,
            final boolean targets
    ) {
        if (current.label().equals(search)) {
            return Optional.of(current);
        } else if (!checked.contains(current.label())) {
            checked.add(current.label());
            return Stream.concat(
                    targets ? current.transitions.stream().filter(Route::hasTarget).map(Route::target) : Stream.empty(),
                    parents ? current.parents.stream() : Stream.empty()
            ).flatMap(q -> find(q, search, checked, parents, targets).stream()).findFirst();
        } else {
            return Optional.empty();
        }
    }

    private void assertSameType(final FlowItem<?, ?> original, final FlowItem<?, ?> invalid) {
        if (original != null && original.getClass() != invalid.getClass()) {
            throw new QuestionTypeException(label, original, invalid);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlowItem<?, ?> that = (FlowItem<?, ?>) o;

        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return label != null ? label.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Question{" +
                "label='" + label + '\'' +
                '}';
    }

    @Override
    public int compareTo(FlowItem o) {
        return String.CASE_INSENSITIVE_ORDER.compare(o.label, label);
    }

}
