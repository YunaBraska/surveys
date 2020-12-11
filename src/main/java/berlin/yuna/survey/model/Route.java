package berlin.yuna.survey.model;

import berlin.yuna.survey.model.types.QuestionGeneric;

import java.util.Objects;
import java.util.function.Function;

public class Route<T> {
    private final QuestionGeneric<?, ?> target;
    private final Function<T, Boolean> function;
    private final Condition<T> condition;

    public Route(final QuestionGeneric<?, ?> target, final Function<T, Boolean> function, final Condition<T> condition) {
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

        Route<?> that = (Route<?>) o;

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