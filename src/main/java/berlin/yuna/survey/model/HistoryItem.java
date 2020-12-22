package berlin.yuna.survey.model;

import berlin.yuna.survey.model.types.QuestionGeneric;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class HistoryItem implements Comparable<HistoryItem> {
    private String label;
    private Object answer;
    private LocalDateTime answeredAt;
    private boolean draft;

    public HistoryItem() {
    }

    public HistoryItem(final String label, final Object answer, final boolean draft, final boolean isAnswered) {
        this.label = label;
        this.answer = answer;
        this.answeredAt = isAnswered ? LocalDateTime.now(ZoneId.of("UTC")) : null;
        this.draft = draft;
    }

    public HistoryItem(final String label) {
        this.label = label;
        this.answer = null;
        this.answeredAt = null;
        this.draft = true;
    }

    public HistoryItem(final HistoryItem answer, final boolean draft) {
        this.label = answer.label;
        this.answer = answer.answer;
        this.answeredAt = answer.answeredAt;
        this.draft = draft;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setAnswer(Object answer) {
        this.answer = answer;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public String getLabel() {
        return label;
    }

    public Object getAnswer() {
        return answer;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public boolean isNotDraft() {
        return !isDraft();
    }

    public boolean isDraft() {
        return draft;
    }

    public boolean isNotAnswered() {
        return !isAnswered();
    }

    public boolean isAnswered() {
        return answeredAt != null;
    }

    public boolean match(final QuestionGeneric<?, ?> question) {
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
                ", answeredAt=" + answeredAt +
                ", draft=" + draft +
                '}';
    }

    @Override
    public int compareTo(final HistoryItem o) {
        if (o.getAnsweredAt() == null) {
            return 1;
        } else if (getAnsweredAt() == null) {
            return -1;
        } else {
            return (o.getAnsweredAt().compareTo(getAnsweredAt()));
        }
    }
}
