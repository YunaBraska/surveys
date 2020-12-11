package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.Condition;
import berlin.yuna.survey.model.HistoryItem;
import berlin.yuna.survey.model.Route;
import berlin.yuna.survey.model.exception.QuestionTypeException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static berlin.yuna.survey.model.exception.QuestionNotFoundException.itemNotFound;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class QuestionGeneric<T, C extends QuestionGeneric<T, C>> implements Comparable<QuestionGeneric<?, ?>> {

    private final String label;
    private Consumer<T> onBack;
    private final Set<QuestionGeneric<?, ?>> parents = ConcurrentHashMap.newKeySet();
    private final Set<Route<T>> routes = ConcurrentHashMap.newKeySet();
    private static final Pattern SPECIAL_CHARS = Pattern.compile("^[A-Z_0-9]*$");

    public QuestionGeneric(final String label) {
        validateNewLabel(label);
        this.label = label;
    }

    /**
     * Get a flow item by the given {@code enum}
     *
     * @param label The {@code label} to search in flow
     * @return Returns {@link Optional<QuestionGeneric>} or {@link Optional#empty()} when flow doesn't contain the
     * requested item
     */
    public Optional<QuestionGeneric<?, ?>> get(final Enum<?> label) {
        return label == null ? Optional.empty() : get(label.name());
    }

    /**
     * Get a flow item by the given {@code String}
     *
     * @param label The {@code label} to search in flow
     * @return Returns {@link Optional<QuestionGeneric>} or {@link Optional#empty()} when flow doesn't contain the
     * requested item
     */
    public Optional<QuestionGeneric<?, ?>> get(final String label) {
        return label == null ? Optional.empty() : find(label);
    }

    /**
     * Get a flow item by the given {@link QuestionGeneric}
     *
     * @param type {@link QuestionGeneric} to search in flow
     * @return Returns {@link Optional<QuestionGeneric>} or {@link Optional#empty()} when flow doesn't contain the
     * requested item
     */
    public <I extends QuestionGeneric<?, ?>> Optional<I> get(final I type) {
        return label == null ? Optional.empty() : find(type);
    }

    /**
     * Get a flow item by this {@code String}
     *
     * @param label    The {@code label} to search in flow
     * @param fallback {@link QuestionGeneric} to return if the flow item wasn't found
     * @return Returns {@link QuestionGeneric} or {@code fallback} when flow doesn't contain the
     * requested item
     */
    public QuestionGeneric<?, ?> getOrElse(final String label, final QuestionGeneric<?, ?> fallback) {
        return label == null ? fallback : get(label).orElse(fallback);
    }

    /**
     * Get a flow item by this {@code enum}
     *
     * @param label    The {@code label} to search in flow
     * @param fallback {@link QuestionGeneric} to return if the flow item wasn't found
     * @return Returns {@link QuestionGeneric} or {@code fallback} when flow doesn't contain the
     * requested item
     */
    public QuestionGeneric<?, ?> getOrElse(final Enum<?> label, final QuestionGeneric<?, ?> fallback) {
        return label == null ? fallback : get(label).orElse(fallback);
    }

    /**
     * Get a flow item by this {@code type}
     *
     * @param type     The {@code label} to search in flow
     * @param fallback {@link QuestionGeneric} to return if the flow item wasn't found
     * @return Returns {@link QuestionGeneric} or {@code fallback} when flow doesn't contain the
     * requested item
     */
    public <I extends QuestionGeneric<?, ?>> I getOrElse(final I type, I fallback) {
        return type == null ? fallback : get(type).orElse(fallback);
    }

    /**
     * Defines the next {@code target} {@link QuestionGeneric} which will come after calling {@link QuestionGeneric#answer()}
     * or {@link QuestionGeneric#answer(Object)}
     *
     * @param target defines the {@code target} which comes after answering
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C target(final QuestionGeneric<?, ?> target) {
        targetGet(target);
        return (C) this;
    }

    /**
     * Defines the next {@code target} {@link QuestionGeneric} which will come after calling {@link QuestionGeneric#answer()} or
     * {@link QuestionGeneric#answer(Object)} when the {@code condition} is given
     *
     * @param target    defines the {@code target} which comes after answering
     * @param condition {@code condition} which mussed be true to route to the specified {@code target}. Can be null
     *                  if no condition needs to be matched see {@link QuestionGeneric#targets()}
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C target(final QuestionGeneric<?, ?> target, final Function<T, Boolean> condition) {
        targetGet(target, condition);
        return (C) this;
    }

    /**
     * Defines the next {@code target} {@link QuestionGeneric} which will come after calling {@link QuestionGeneric#answer()} or
     * {@link QuestionGeneric#answer(Object)} when the {@code condition} is given
     *
     * @param target    defines the {@code target} which comes after answering
     * @param condition {@code condition} which mussed be true to route to the specified {@code target}. Can be null
     *                  if no condition needs to be matched see {@link QuestionGeneric#targets()}
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C target(final QuestionGeneric<?, ?> target, final Condition<T> condition) {
        targetGet(target, condition);
        return (C) this;
    }

    /**
     * Defines the next {@code target} {@link QuestionGeneric} which will come after calling {@link QuestionGeneric#answer()}
     * or {@link QuestionGeneric#answer(Object)}
     *
     * @param target defines the {@code target} which comes after answering
     * @return returns the {@code target} object
     */
    public <I extends QuestionGeneric<?, ?>> I targetGet(final I target) {
        return targetGet(target, null, null);
    }

    /**
     * Defines the next {@code target} {@link QuestionGeneric} which will come after calling {@link QuestionGeneric#answer()}
     * or {@link QuestionGeneric#answer(Object)}
     *
     * @param target    defines the {@code target} which comes after answering
     * @param condition {@code condition} which mussed be true to route to the specified {@code target}. Can be null
     *                  if no condition needs to be matched see {@link QuestionGeneric#targets()}
     * @return returns the {@code target} object
     */
    public <I extends QuestionGeneric<?, ?>> I targetGet(final I target, final Function<T, Boolean> condition) {
        return targetGet(target, null, condition);
    }

    /**
     * Defines the next {@code target} {@link QuestionGeneric} which will come after calling {@link QuestionGeneric#answer()}
     * or {@link QuestionGeneric#answer(Object)}
     *
     * @param target    defines the {@code target} which comes after answering
     * @param condition {@code condition} which mussed be true to route to the specified {@code target}. Can be null
     *                  if no condition needs to be matched see {@link QuestionGeneric#targets()}
     * @return returns the {@code target} object
     */
    public <I extends QuestionGeneric<?, ?>> I targetGet(final I target, final Condition<T> condition) {
        return targetGet(target, condition, null);
    }

    /**
     * Returns a {@link Set} with all configured targets
     *
     * @return a set view of all configured {@code targets} for this {@link QuestionGeneric} object
     */
    public Set<QuestionGeneric<?, ?>> targets() {
        return routes.stream().map(Route::target).collect(Collectors.toSet());
    }

    /**
     * Returns a {@link Set} with all configured targets
     *
     * @return a set view of all configured {@code targets} for this {@link QuestionGeneric} object
     */
    public Set<QuestionGeneric<?, ?>> parents() {
        return new HashSet<>(parents);
    }

    /**
     * Answers the question with {@code null} and returns next question
     *
     * @return empty if there is no next question configured
     */
    public Optional<QuestionGeneric<?, ?>> answer() {
        return answer(null);
    }

    /**
     * Converts input to the answer type for {@link QuestionGeneric#answer(Object)}
     * Its also used within {@link QuestionGeneric#parseAndAnswer(Object)}
     *
     * @param answer input parameter to be parsed
     *               {@link QuestionGeneric#target(QuestionGeneric)} (Object)}
     * @return depends on the implementation - null usually means its not parsable which needs to be cached at
     */
    public abstract Optional<T> parse(final Object answer);

    /**
     * Converts input and returns next {@link QuestionGeneric} which is configured fot the {@code answer} at
     * {@link QuestionGeneric#target(QuestionGeneric)} (Object)}
     *
     * @param answer input parameter
     * @return {@link Optional#empty()} when question doesn't match configuration - see {@link QuestionGeneric#target(QuestionGeneric)} (Object)}
     */
    public Optional<QuestionGeneric<?, ?>> parseAndAnswer(final Object answer) {
        return parse(answer).flatMap(this::answer);
    }

    /**
     * Returns next {@link QuestionGeneric} which is configured fot the {@code answer} at
     * {@link QuestionGeneric#target(QuestionGeneric)} (Object)}
     *
     * @param answer input parameter
     * @return returns next {@link QuestionGeneric} which is configured at {@link QuestionGeneric#target(QuestionGeneric)} (Object)}
     */
    public Optional<QuestionGeneric<?, ?>> answer(final T answer) {
        if (answer != null) {
            for (Route<T> route : routes) {
                if (route.hasCondition() && route.apply(answer)) {
                    return Optional.of(route.target());
                }
            }
        }
        return routesWithoutCondition();
    }

    /**
     * @return label
     */
    public String label() {
        return label;
    }

    /**
     * @param answer input for check
     * @return {@code true} if {@link HistoryItem} relates to the current {@link QuestionGeneric}
     */
    public boolean match(final HistoryItem answer) {
        return answer != null && label.equals(answer.getLabel());
    }

    /**
     * Triggers the on back function which is configured at {@link QuestionGeneric#onBack(Consumer)}
     *
     * @param answer input for on back checks
     * @return {@code true} on default or {@code false} when configured at {@link QuestionGeneric#onBack(Consumer)}
     */
    public boolean onBack(Object answer) {
        if (onBack != null) {
            onBack.accept(parse(answer).orElse(null));
        }
        //FIXME: implement on back function
        return true;
    }

    /**
     * Defines what happens when a back transition is requested
     *
     * @param onBack function which decides what happens when back transition is triggered see {@link QuestionGeneric#onBack(Object)}
     * @return returns the current object
     */
    @SuppressWarnings("unchecked")
    public C onBack(final Consumer<T> onBack) {
        this.onBack = onBack;
        return (C) this;
    }

    /**
     * @return a copy of configured routes
     */
    public Set<Route<T>> routes() {
        return new HashSet<>(routes);
    }

    /**
     * @return the route without condition id configured else empty
     */
    private Optional<QuestionGeneric<?, ?>> routesWithoutCondition() {
        return routes.stream().filter(Route::hasNoCondition).findFirst().map(Route::target);
    }

    private static void validateNewLabel(final String label) {
        if (!SPECIAL_CHARS.matcher(label).find()) {
            throw new IllegalArgumentException("Label should only contain enum able characters like [A-Z_0-9]");
        }
    }

    @SuppressWarnings({"unchecked"})
    protected void addTarget(final Route<?> route) {
        routes.add((Route<T>) route);
    }

    protected void addParent(final QuestionGeneric<?, ?> parent) {
        parents.add(parent);
    }

    private void removeTargets(final Predicate<Route<T>> filter) {
        final Set<Route<T>> connections = routes.stream().filter(filter).collect(Collectors.toSet());
        connections.forEach(route -> route.target().parents.remove(this));
        routes.removeAll(connections);
    }

    private Optional<QuestionGeneric<?, ?>> find(final String search) {
        return find(this, search, new HashSet<>(), true, true);
    }

    // LOGIC

    private <I extends QuestionGeneric<?, ?>> I targetGet(final I target, final Condition<T> condition, final Function<T, Boolean> function) {
        if (target == null) {
            throw itemNotFound(null, label);
        }

        final I flowTarget = find(target).orElse(target);
        if (condition == null && function == null) {
            removeTargets(Route::hasNoCondition);
        }

        //merge
        target.routes().forEach(flowTarget::addTarget);
        target.parents().forEach(flowTarget::addParent);

        //add route to patent and child
        routes.add(new Route<>(flowTarget, function, condition));
        flowTarget.addParent(this);
        return flowTarget;
    }

    @SuppressWarnings("unchecked")
    private <I extends QuestionGeneric<?, ?>> Optional<I> find(final I search) {
        Optional<QuestionGeneric<?, ?>> result = find(search.label());
        assertSameType(result.orElse(null), search);
        return result.isEmpty() ? Optional.empty() : Optional.of((I) result.get());
    }

    private Optional<QuestionGeneric<?, ?>> find(
            final QuestionGeneric<?, ?> current,
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
                    targets ? current.routes.stream().map(Route::target) : Stream.empty(),
                    parents ? current.parents.stream() : Stream.empty()
            ).flatMap(q -> find(q, search, checked, parents, targets).stream()).findFirst();
        } else {
            return Optional.empty();
        }
    }

    private void assertSameType(final QuestionGeneric<?,?> original, final QuestionGeneric<?,?> invalid) {
        if (original != null && original.getClass() != invalid.getClass()) {
            throw new QuestionTypeException(label, original, invalid);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuestionGeneric<?, ?> that = (QuestionGeneric<?, ?>) o;

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
    public int compareTo(QuestionGeneric o) {
        return String.CASE_INSENSITIVE_ORDER.compare(o.label, label);
    }

}
