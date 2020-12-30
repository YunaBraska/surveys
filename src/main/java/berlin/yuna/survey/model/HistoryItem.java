package berlin.yuna.survey.model;

import berlin.yuna.survey.model.types.FlowItem;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class HistoryItem implements Comparable<HistoryItem> {
    private String label;
    private Object answer;
    private LocalDateTime createdAt;
    private State state = State.CURRENT;

    public HistoryItem() {
        this.createdAt = LocalDateTime.now(ZoneId.of("UTC"));
    }

    public HistoryItem(final String label, final Object answer, final State state) {
        this(label);
        this.answer = answer;
        this.state = state;
    }

    public HistoryItem(final String label) {
        this();
        this.label = label;
        this.answer = null;
        this.state = State.DRAFT;
    }

    public HistoryItem setLabel(String label) {
        this.label = label;
        return this;
    }

    public HistoryItem setAnswer(Object answer) {
        this.answer = answer;
        return this;
    }

    public HistoryItem setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Object getAnswer() {
        return answer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public State getState() {
        return state;
    }

    public HistoryItem setState(final State state) {
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

        HistoryItem that = (HistoryItem) o;

        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return label != null ? label.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "QuestionAnswer{" +
                "label='" + label + '\'' +
                ", answer='" + answer + '\'' +
                ", createdAt=" + createdAt +
                ", state='" + state + '\'' +
                '}';
    }

    @Override
    public int compareTo(final HistoryItem o) {
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
