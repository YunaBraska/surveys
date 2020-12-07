package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.Condition;
import berlin.yuna.survey.model.HistoryItem;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class QuestionGeneric<T, C extends QuestionGeneric<T, C>> implements Comparable<QuestionGeneric<?, ?>> {

    private final String label;
    private Consumer<T> onBack;
    private final Set<AnswerRoute<T>> routes = ConcurrentHashMap.newKeySet();
    private static final Pattern SPECIAL_CHARS = Pattern.compile("^[A-Z_0-9]*$");
    private static final Map<String, QuestionGeneric<?, ?>> all = new ConcurrentHashMap<>();

    /**
     * Returns the specified {@link QuestionGeneric} which is the specified by this label
     *
     * @param label The label which is configured  for the specified {@code label}
     * @return {@link QuestionGeneric} found by the {@code label} or {@code null} if there is no configuration for it
     */
    public static QuestionGeneric<?, ?> get(final Enum<?> label) {
        return label == null ? null : get(label.name());
    }

    /**
     * Returns the specified {@link QuestionGeneric} which is the specified by this label
     *
     * @param label The label which is configured  for the specified {@code label}
     * @return {@link QuestionGeneric} found by the {@code label} or {@code null} if there is no configuration for it
     */
    public static QuestionGeneric<?, ?> get(final String label) {
        return label == null ? null : all.get(label);
    }

    /**
     * Returns the specified {@link QuestionGeneric} which is the specified by this label
     *
     * @param label The label which is configured  for the specified {@code label}
     * @param fallback if no question was found {@code label}
     * @return {@link QuestionGeneric} found by the {@code label} or {@code null} if there is no configuration for it
     */
    public static QuestionGeneric<?, ?> getOrElse(final String label, final QuestionGeneric<?, ?> fallback) {
        return label == null ? fallback : all.getOrDefault(label, fallback);
    }

    /**
     * Removes all of the configured mappings
     */
    public static void clearAll() {
        all.clear();
    }

    /**
     * @param label label to check for
     * @return {@code true} if there is a configuration for the specified {@code label}
     * {@link QuestionGeneric} exists
     */
    public static boolean exists(final Enum<?>... label) {
        return exists(stream(label).map(Enum::name).toArray(String[]::new));
    }

    /**
     * Checks if a question is linked in the question flow
     *
     * @param question {@link QuestionGeneric} to check if its contained in the flow
     * @return true if the {@code question} is linked in current {@link QuestionGeneric} flow
     */
    public boolean containsTarget(final QuestionGeneric<?, ?> question) {
        return containsTarget(this, question);
    }

    /**
     * Checks if a question is linked in the question flow
     *
     * @param flowStart {@link QuestionGeneric} of the flow start
     * @param question  {@link QuestionGeneric} to check if its contained in the flow
     * @return true if the {@code question} is linked in {@code flowStart}
     */
    public static boolean containsTarget(final QuestionGeneric<?, ?> flowStart, final QuestionGeneric<?, ?> question) {
        return containsTarget(flowStart, question, new HashSet<>());
    }

    /**
     * Returns a set of parent {@link QuestionGeneric}
     *
     * @param question {@link QuestionGeneric} to find the parent of in the flow
     * @return a set of parents - set will be empty if no parent was found
     */
    public Set<QuestionGeneric<?, ?>> getParentsOf(final QuestionGeneric<?, ?> question) {
        return getParentsOf(this, question);
    }

    /**
     * Returns a set of parent {@link QuestionGeneric}
     *
     * @param flowStart {@link QuestionGeneric} of the flow start
     * @param question  {@link QuestionGeneric} to find the parent of in the flow
     * @return a set of parents - set will be empty if no parent was found
     */
    public static Set<QuestionGeneric<?, ?>> getParentsOf(final QuestionGeneric<?, ?> flowStart, final QuestionGeneric<?, ?> question) {
        final Set<QuestionGeneric<?, ?>> result = new HashSet<>();
        if(!flowStart.equals(question)) {
            getParentsOf(flowStart, question, new HashSet<>(), result);
        }
        return result;
    }

    private static void getParentsOf(final QuestionGeneric<?, ?> current, final QuestionGeneric<?, ?> search, final Set<QuestionGeneric<?, ?>> checked, final Set<QuestionGeneric<?, ?>> result) {
        if (current.target().contains(search)) {
            result.add(current);
        } else if (!checked.contains(current)) {
            checked.add(current);
            current.target().forEach(q -> getParentsOf(q, search, checked, result));
        }
    }

    /**
     * @param label label to check for
     * @return {@code true} if there is a configuration for the specified {@code label}
     * {@link QuestionGeneric} exists
     */
    public static boolean exists(final String... label) {
        return all.keySet().containsAll(Set.of(label));
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
     *                  if no condition needs to be matched see {@link QuestionGeneric#target()}
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
     *                  if no condition needs to be matched see {@link QuestionGeneric#target()}
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
     *                  if no condition needs to be matched see {@link QuestionGeneric#target()}
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
     *                  if no condition needs to be matched see {@link QuestionGeneric#target()}
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
    public Set<QuestionGeneric<?, ?>> target() {
        return routes.stream().map(AnswerRoute::target).collect(Collectors.toSet());
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
            for (AnswerRoute<T> route : routes) {
                if (route.hasCondition() && route.apply(answer)) {
                    return Optional.of(route.target());
                }
            }
        }
        return getTargetWithoutCondition();
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

    public Set<AnswerRoute<T>> routes() {
        return new HashSet<>(routes);
    }
//
//    @SuppressWarnings("unchecked")
//    public C routes(final Set<AnswerRoute<T>> answerRoutes) {
//        routes.clear();
//        this.routes.addAll(answerRoutes);
//        return (C) this;
//    }

    protected QuestionGeneric(final String label) {
        validateNewLabel(label);
        this.label = label;
        all.put(label, this);
    }

    protected static <K> K getOrNew(
            final String label,
            final Class<K> type,
            final Supplier<K> instance
    ) {
        QuestionGeneric<?, ?> result = all.get(label);
        if (label == null) {
            return null;
        } else if (result == null) {
            return instance.get();
        } else if (result.getClass() == type) {
            return type.cast(result);
        } else {
            throw new IllegalStateException(format(
                    "Found question [%s] with different type [%s] than requested [%s]",
                    label,
                    ofNullable(get(label)).map(c -> c.getClass().getSimpleName()).orElse(null),
                    type.getSimpleName()
            ));
        }
    }

    private static boolean containsTarget(final QuestionGeneric<?, ?> current, final QuestionGeneric<?, ?> search, final Set<QuestionGeneric<?, ?>> checked) {
        if (search.equals(current)) {
            return true;
        } else if (!checked.contains(current)) {
            checked.add(current);
            return current.target().stream().anyMatch(q -> containsTarget(q, search, checked));
        } else {
            return false;
        }
    }

    private Optional<QuestionGeneric<?, ?>> getTargetWithoutCondition() {
        return routes.stream().filter(AnswerRoute::hasNoCondition).findFirst().map(AnswerRoute::target);
    }

    private static void validateNewLabel(final String label) {
        if (!SPECIAL_CHARS.matcher(label).find()) {
            throw new IllegalArgumentException("Label should only contain enum able characters like [A-Z_0-9]");
        }
    }

    private <I extends QuestionGeneric<?, ?>> I targetGet(final I target, final Condition<T> condition, final Function<T, Boolean> function) {
        if (condition == null && function == null) {
            routes.removeAll(routes.stream().filter(AnswerRoute::hasNoCondition).collect(Collectors.toSet()));
        }
        routes.add(new AnswerRoute<>(target, function, condition));
        return target;
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

    public static class AnswerRoute<T> {
        private final QuestionGeneric<?, ?> target;
        private final Function<T, Boolean> function;
        private final Condition<T> condition;

        public AnswerRoute(final QuestionGeneric<?, ?> target, final Function<T, Boolean> function, final Condition<T> condition) {
            this.target = target;
            this.condition = condition;
            this.function = function;
        }

        public boolean apply(T answer) {
            return (hasChoice() && condition.apply(answer)) || (hasFunction() && function.apply(answer));
        }

        public boolean hasChoice() {
            return condition != null;
        }

        public boolean hasFunction() {
            return function != null;
        }

        public boolean hasCondition() {
            return hasChoice() || hasFunction();
        }

        public boolean hasNoCondition() {
            return !hasCondition();
        }

        public String getLabel() {
            return hasChoice() ? (condition.getLabel() != null ? condition.getLabel() : condition.getClass().getSimpleName()) : null;
        }

        public QuestionGeneric<?, ?> target() {
            return target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AnswerRoute<?> that = (AnswerRoute<?>) o;

            if (!Objects.equals(target, that.target)) return false;
            if (!Objects.equals(function, that.function)) return false;
            return Objects.equals(condition, that.condition);
        }

        @Override
        public int hashCode() {
            int result = target != null ? target.hashCode() : 0;
            result = 31 * result + (function != null ? function.hashCode() : 0);
            result = 31 * result + (condition != null ? condition.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "AnswerRoute{" +
                    "target=" + target +
                    ", function=" + function +
                    ", choice=" + getLabel() +
                    '}';
        }
    }
}
