package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.Condition;
import berlin.yuna.survey.model.HistoryItem;
import berlin.yuna.survey.model.HistoryItemBase;
import berlin.yuna.survey.model.HistoryItemJson;
import berlin.yuna.survey.model.types.FlowItem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static berlin.yuna.survey.model.HistoryItemBase.State.ANSWERED;
import static berlin.yuna.survey.model.HistoryItemBase.State.CURRENT;
import static berlin.yuna.survey.model.HistoryItemBase.State.DRAFT;
import static berlin.yuna.survey.model.exception.QuestionNotFoundException.itemNotFound;
import static berlin.yuna.survey.model.exception.QuestionNotFoundException.itemNotFoundInHistory;
import static java.util.stream.Collectors.toCollection;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Survey {

    private FlowItem<?, ?> last;
    private FlowItem<?, ?> flowStart;
    private boolean autoBackTransition = true;
    //https://stackoverflow.com/questions/4724995/lock-free-concurrent-linked-list-in-java
    private final LinkedList<HistoryItem> history = new LinkedList<>();

    /**
     * Starts new SurveyCtx
     *
     * @param flowStart start item of the flow
     * @return {@link Survey}
     * @throws IllegalStateException on {@code null}
     */
    public static Survey init(final FlowItem<?, ?> flowStart) {
        return new Survey(flowStart);
    }

    /**
     * Continues {@link Survey} from a history
     * Removes all invalid {@link FlowItem} items from the history
     *
     * @param flowStart start item of the flow
     * @param history   should not be empty as {@link Survey} needs a start item
     * @return {@link Survey}
     * @throws IllegalStateException when the {@code history} is empty or has no valid {@link FlowItem}
     */
    public static Survey init(final FlowItem<?, ?> flowStart, final Iterable<? extends HistoryItemBase<?>> history) {
        final LinkedList<HistoryItem> linkedHistory = StreamSupport.stream(history.spliterator(), false)
                .map(item -> HistoryItem.of(flowStart, item))
                .flatMap(Optional::stream)
                .collect(toCollection(LinkedList::new));
        Survey context = init(flowStart);
        if (linkedHistory.isEmpty()) {
            return init(flowStart);
        }
        context.history.clear();
        context.history.addAll(linkedHistory);
        context.last = context.findLast(linkedHistory);
        context.flowStart = context.findFirst();
        return context;
    }

    /**
     * Transit to a specific {@link FlowItem} in the flow
     *
     * @param label for {@link FlowItem} to transition to
     * @return {@code true} if transition is allowed, {@code false} on back transition config
     * @throws IllegalArgumentException if the label is not part of the flow or when the forward transition has not enough answers
     */
    public boolean transitTo(final String label) {
        return transitTo(last.get(label).orElseThrow(() -> itemNotFound(label, flowStart.label())));
    }

    /**
     * Transit to a specific {@link FlowItem} in the flow
     *
     * @param target {@link FlowItem} to transition to
     * @return {@code true} if transition is allowed, {@code false} on config of {@link FlowItem#onBack(Condition[])}
     * @throws IllegalArgumentException if the label is not part of the flow or when the forward transition has not
     *                                  enough answers (will transition to the nearest possible {@link FlowItem})
     */
    public boolean transitTo(final FlowItem<?, ?> target) {
        if (target.equals(get())) {
            return true;
        }
        boolean result = true;
        assertQuestionBelongsToFlow(target);

        if (history.stream().filter(HistoryItem::isNotDraft).anyMatch(target::match)) {
            result = runBackTransitions(target);
        } else {
            runForwardTransitions(target);
        }
        return result;
    }

    /**
     * Get current {@link FlowItem} of the flow
     *
     * @return {@link FlowItem} of the current flow
     */
    public FlowItem<?, ?> get() {
        return last;
    }

    /**
     * Get a flow item by the given {@code String}
     * To avoid cast its recommended to use {@link FlowItem#get(FlowItem)}
     *
     * @param label The {@code label} to search in flow
     * @return Returns {@link Optional<FlowItem>} or {@code null} when flow doesn't contain the
     * requested item
     */
    public FlowItem<?, ?> get(final String label) {
        return flowStart.get(label).orElse(null);
    }

    /**
     * Get a flow item by the given {@code enum}
     * To avoid cast its recommended to use {@link FlowItem#get(FlowItem)}
     *
     * @param label The {@code label} to search in flow
     * @return Returns {@link FlowItem} or {@code null} when flow doesn't contain the
     * requested item
     */
    public FlowItem<?, ?> get(final Enum<?> label) {
        return flowStart.get(label).orElse(null);
    }

    /**
     * Get a flow item by the given {@link FlowItem}
     *
     * @param type {@link FlowItem} to search in flow
     * @return Returns {@link FlowItem} or {@code null} when flow doesn't contain the
     * requested item
     */
    public <I extends FlowItem<?, ?>> I get(final I type) {
        return flowStart.get(type).orElse(null);
    }

    /**
     * Get previous {@link FlowItem} from the flow
     *
     * @return previous {@link FlowItem} and {@code null} if there is no previous entry
     */
    public FlowItem<?, ?> getPrevious() {
        return last.parents().stream().filter(q -> getHistoryAnswered().anyMatch(item -> item.match(q))).findFirst().orElse(null);
    }

    /**
     * Get first {@link FlowItem} of the flow
     *
     * @return first {@link FlowItem} of the current flow
     */
    public FlowItem<?, ?> getFirst() {
        return flowStart;
    }

    /**
     * Check if the current flow has ended
     *
     * @return true if there is no next {@link FlowItem}
     */
    public boolean isEnded() {
        return last.targets().isEmpty() && getHistoryAnswered().filter(HistoryItem::isNotDraft).anyMatch(answer -> answer.match(last));
    }

    /**
     * Get history of answers
     *
     * @return all answers which were given in the context
     */
    public List<HistoryItem> getHistory() {
        return new LinkedList<>(history);
    }

    /**
     * Get history of answers
     *
     * @return all answers as json format which were given in the context
     */
    public List<HistoryItemJson> getHistoryJson() {
        return history.stream().map(item -> HistoryItemJson.of(flowStart, item)).flatMap(Optional::stream).collect(toCollection(LinkedList::new));
    }

    /**
     * Get history size
     *
     * @return number of given answers
     */
    public int getHistorySize() {
        return history.size();
    }

    /**
     * Solves the current {@link FlowItem} of the flow
     *
     * @return {@link Survey}
     */
    public Survey answer(final Object answer) {
        return answer(answer, true);
    }

    private Survey answer(final Object answer, final boolean upDate) {
        Optional<FlowItem<?, ?>> result = last.parseAndAnswer(answer);
        markAsAnswered(last.label(), answer, upDate);
        if (result.isPresent()) {
            last = result.get();
            if (upDate && !isEnded()) {
                markAsCurrent(last.label());
            }
        }
        return this;
    }

    /**
     * Returns times taken between each question useful to track answer times
     *
     * @return times of answered questions
     */
    public Map<String, Long> getDurationsMS() {
        final Map<String, Long> result = new LinkedHashMap<>();
        final AtomicReference<HistoryItem> lastTime = new AtomicReference<>(null);
        getHistoryAnswered().sorted().filter(HistoryItem::isNotDraft).forEach(item -> {
            Optional.ofNullable(lastTime.get()).ifPresent(
                    lastT -> result.put(lastT.getLabel(), Duration.between(lastT.getCreatedAt(), item.getCreatedAt()).toMillis())
            );
            lastTime.set(item);
        });
        return result;
    }

    /**
     * Defines if back transitions are allowed for non configured back conditions
     *
     * @param enableAutomatic {@code true} on default
     * @return {@link Survey}
     */
    public Survey autoBackTransition(final boolean enableAutomatic) {
        autoBackTransition = enableAutomatic;
        return this;
    }

    /**
     * Definition if back transitions are allowed for non configured back conditions
     *
     * @return {@code true} on default
     */
    public boolean hasAutoBackTransition() {
        return autoBackTransition;
    }

    /**
     * Prepares diagram renderer
     *
     * @return {@link DiagramExporter} renderer
     */
    public DiagramExporter diagram() {
        return new DiagramExporter(this);
    }

    protected FlowItem<?, ?> findLast(final LinkedList<HistoryItem> historySorted) {
        final String label = historySorted.stream()
                .filter(HistoryItem::isNotAnswered).findFirst()
                .map(HistoryItem::getLabel)
                .orElse(historySorted.getLast().getLabel());
        return flowStart.get(label).orElseThrow(() -> itemNotFoundInHistory(label, flowStart.label()));
    }

    /**
     * Find first {@link FlowItem} of the flow
     *
     * @return first {@link FlowItem} of the current flow
     */
    private FlowItem<?, ?> findFirst() {
        return flowStart.get(history.getFirst().getLabel()).orElseThrow(() -> itemNotFound(history.getFirst().getLabel(), flowStart.label()));
    }

    private Stream<HistoryItem> getHistoryAnswered() {
        return history.stream().filter(HistoryItem::isAnswered);
    }

    private void markAsCurrent(final String label) {
        final HistoryItem historyItem = getOrCreateAnswer(label);
        historyItem.setState(CURRENT);
    }

    private void markAsDraft(final String label) {
        getOrCreateAnswer(label).setState(DRAFT);
    }

    private void markAsAnswered(final String label, final Object answer, final boolean upDate) {
        final HistoryItem historyItem = getOrCreateAnswer(label);
        if (upDate || historyItem.isNotAnswered()) {
            historyItem.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        }
        historyItem.setState(ANSWERED);
        historyItem.setAnswer(answer);
    }

    private HistoryItem getOrCreateAnswer(final String label) {
        final HistoryItem answer = new HistoryItem(label);
        int index = history.indexOf(answer);
        if (index == -1) {
            history.add(answer);
            return answer;
        }
        return history.get(index);
    }

    private Survey(final FlowItem<?, ?> startQuestion) {
        assertExists(startQuestion);
        this.last = startQuestion;
        markAsCurrent(last.label());
        flowStart = startQuestion;
    }

    private void assertExists(FlowItem<?, ?> startQuestion) {
        if (startQuestion == null) {
            throw new IllegalArgumentException("Missing " + FlowItem.class.getSimpleName() + ", given was null");
        }
    }


    private void runForwardTransitions(final FlowItem<?, ?> target) {
        final Set<String> checkedLabel = new HashSet<>();
        final Map<String, Object> mappedHistory = getHistoryAnswered().collect(toLinkedMap(HistoryItem::getLabel, HistoryItem::getAnswer));
        String label = flowStart.label();
        do {
            final FlowItem<?, ?> currentQuestion = answer(mappedHistory.get(label), false).get();
            label = currentQuestion.label();
            if (checkedLabel.contains(label)) {
                //FIXME: custom checked exception
                throw new IllegalArgumentException("Unable transition to [" + target.label() + "]" + " Answer from the history did not solved [" + label + "]");
            }
            checkedLabel.add(label);
            last = currentQuestion;
        } while (!label.equals(target.label()));
        if (mappedHistory.containsKey(target.label())) {
            answer(label, false);
        }
    }

    private boolean runBackTransitions(final FlowItem<?, ?> question) {
        final Iterator<HistoryItem> iterator = new LinkedList<>(history).descendingIterator();
        while (iterator.hasNext()) {
            HistoryItem answer = iterator.next();
            if (answer.isCurrent()) {
                history.remove(answer);
                continue;
            }
            if (answer.getLabel().equals(question.label())) {
                markAsCurrent(answer.getLabel());
                last = flowStart.getOrElse(answer.getLabel(), last);
                return true;
            }
            final boolean revertIsAllowed = flowStart.get(answer.getLabel()).flatMap(q -> q.parseAndRevert(answer.getAnswer())).orElse(autoBackTransition);
            if (revertIsAllowed) {
                markAsDraft(answer.getLabel());
                last = flowStart.getOrElse(answer.getLabel(), last);
            } else {
                markAsCurrent(last.label());
                return false;
            }
        }
        markAsCurrent(last.label());
        return true;
    }

    private void assertQuestionBelongsToFlow(final FlowItem<?, ?> question) {
        assertExists(question);
        if (flowStart.get(question.label()).isEmpty()) {
            throw itemNotFoundInHistory(question.label(), flowStart.label());
        }
    }

    private static <T, K, U> Collector<T, ?, Map<K, U>> toLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new
        );
    }
}
