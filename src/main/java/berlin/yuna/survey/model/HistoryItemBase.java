package berlin.yuna.survey.model;

import berlin.yuna.survey.model.types.FlowItem;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import static berlin.yuna.survey.logic.CommonUtils.getTime;

/**
 * The {@link HistoryItem} is used to keep track of all answers/transitions in the flow
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class HistoryItemBase<T> implements Comparable<HistoryItemBase> {
    private String label;
    private T answer;
    private LocalDateTime createdAt;
    private State state = State.CURRENT;

    public HistoryItemBase() {
        this.createdAt = getTime();
    }

    public HistoryItemBase(final String label, final T answer, final State state) {
        this(label);
        this.answer = answer;
        this.state = state;
    }

    public HistoryItemBase(final String label) {
        this();
        this.label = label;
        this.answer = null;
        this.state = State.DRAFT;
    }

    protected HistoryItemBase(final String label, final T answer, final LocalDateTime createdAt, final State state) {
        this.label = label;
        this.answer = answer;
        this.createdAt = createdAt;
        this.state = state;
    }

    public HistoryItemBase<T> setLabel(String label) {
        this.label = label;
        return this;
    }

    public HistoryItemBase<T> setAnswer(T answer) {
        this.answer = answer;
        return this;
    }

    public HistoryItemBase<T> setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public T getAnswer() {
        return answer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public State getState() {
        return state;
    }

    public HistoryItemBase<T> setState(final State state) {
        this.state = state;
        return this;
    }

    public boolean isNotDraft() {
        return !isDraft();
    }

    public boolean isDraft() {
        return state == State.DRAFT;
    }

    public boolean isNotCurrent() {
        return !isCurrent();
    }

    public boolean isCurrent() {
        return state == State.CURRENT;
    }

    public boolean isNotAnswered() {
        return !isAnswered();
    }

    public boolean isAnswered() {
        return answer != null;
    }

    public boolean match(final FlowItem<?, ?> question) {
        return question != null && question.label().equals(label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryItemBase<?> that = (HistoryItemBase<?>) o;

        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return label != null ? label.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "label='" + label + '\'' +
                ", answer='" + answer + '\'' +
                ", createdAt=" + createdAt +
                ", state='" + state + '\'' +
                '}';
    }

    @Override
    public int compareTo(final HistoryItemBase o) {
        if (o.getCreatedAt() == null) {
            return 1;
        } else if (getCreatedAt() == null) {
            return -1;
        } else {
            return (o.getCreatedAt().compareTo(getCreatedAt()));
        }
    }

    public enum State {
        ANSWERED,
        CURRENT,
        DRAFT,
    }
}
