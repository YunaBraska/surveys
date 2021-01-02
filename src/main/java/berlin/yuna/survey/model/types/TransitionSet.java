package berlin.yuna.survey.model.types;

import berlin.yuna.survey.model.Condition;
import berlin.yuna.survey.model.Route;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static berlin.yuna.survey.model.exception.QuestionNotFoundException.itemNotFound;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class TransitionSet<T> extends HashSet<Route<T>> {

    private final FlowItem<T, ?> flowItem;

    public TransitionSet(FlowItem<T, ?> flowItem) {
        this.flowItem = flowItem;
    }

    public Set<Route<T>> forwardRoutes() {
        return getRouteStream(Route::isForwardFlow).collect(toSet());
    }

    public Set<Route<T>> backwardRoutes() {
        return getRouteStream(Route::isBackwardFlow).collect(toSet());
    }

    public Set<FlowItem<?, ?>> forwardTargets() {
        return getRouteStream(Route::isForwardFlow).map(Route::target).collect(toSet());
    }

    public Set<FlowItem<?, ?>> backwardTargets() {
        return getRouteStream(Route::isBackwardFlow).map(Route::target).collect(toSet());
    }

    private Stream<Route<T>> getRouteStream(final Predicate<Route<T>> filter) {
        return stream().filter(filter);
    }

    protected FlowItem<T, ?> backCondition(final Condition<T> condition, final Function<T, Boolean> function) {
        removeItemsWithoutCondition(condition, function, true);
        add(new Route<>(null, function, condition, true));
        return flowItem;
    }

    protected <I extends FlowItem<?, ?>> I pointToAndGet(final I target, final Condition<T> condition, final Function<T, Boolean> function) {
        if (target == null) {
            throw itemNotFound(null, flowItem.label());
        }

        removeItemsWithoutCondition(condition, function, false);

        final I flowTarget = flowItem.find(target).orElse(target);

        //merge
        flowTarget.transitions().addRoutes(target.transitions());
        target.parents().forEach(flowTarget::addParent);

        //add route to patent and child
        add(new Route<>(flowTarget, function, condition, false));
        flowTarget.addParent(flowItem);
        return flowTarget;
    }

    @SuppressWarnings("unchecked")
    private void addRoutes(final TransitionSet<?> transitionSet) {
        addAll((Collection<? extends Route<T>>) transitionSet);
    }

    private void removeItemsWithoutCondition(final Condition<T> condition, final Function<T, ?> function, boolean isBack) {
        if (condition == null && function == null) {
            final Set<Route<T>> connections = stream().filter(route -> isBack? route.isBackwardFlow() : route.isForwardFlow()).filter(Route::hasNoCondition).collect(toSet());
            connections.forEach(route -> route.target().parents().remove(flowItem));
            removeAll(connections);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TransitionSet<?> that = (TransitionSet<?>) o;

        return Objects.equals(flowItem, that.flowItem);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (flowItem != null ? flowItem.hashCode() : 0);
        return result;
    }
}
