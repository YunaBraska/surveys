package berlin.yuna.survey.model;

import berlin.yuna.survey.model.types.FlowItem;

import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Route<T> {
    private final FlowItem<?, ?> target;
    private final Function<T, Boolean> function;
    private final Condition<T> condition;
    private final boolean backwards;

    public Route(final FlowItem<?, ?> target, final Function<T, Boolean> function, final Condition<T> condition, final boolean backwards) {
        this.target = target;
        this.condition = condition;
        this.function = function;
        this.backwards = backwards;
    }

    public boolean apply(T answer) {
        return (hasChoice() && condition.apply(answer)) || (hasFunction() && function.apply(answer));
    }

    public boolean hasChoice() {
        return condition != null;
    }

    public boolean hasTarget() {
        return target != null;
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

    public FlowItem<?, ?> target() {
        return target;
    }

    public Condition<T> getCondition() {
        return condition;
    }

    public boolean isBackwardFlow() {
        return backwards;
    }

    public boolean isForwardFlow() {
        return !isBackwardFlow();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Route<?> route = (Route<?>) o;

        if (backwards != route.backwards) return false;
        if (!Objects.equals(function, route.function)) return false;
        return Objects.equals(condition, route.condition);
    }

    @Override
    public int hashCode() {
        int result = function != null ? function.hashCode() : 0;
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (backwards ? 1 : 0);
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