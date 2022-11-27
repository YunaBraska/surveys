package berlin.yuna.survey.model;

import berlin.yuna.survey.logic.Survey;
import berlin.yuna.survey.model.types.FlowItem;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static berlin.yuna.survey.logic.CommonUtils.getTime;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ContextExchange {

    private Object context;
    private Survey survey;
    private FlowItem<?, ?> target;
    private final UUID id = UUID.randomUUID();
    private final Object payload;
    private final LocalDateTime dateTime = getTime();
    private final Map<String, Object> metaData = new HashMap<>();

    public static ContextExchange contextOf(final Survey survey, final Object payload, final Object context, final FlowItem<?, ?> target) {
        return contextOf(survey, payload, context).target(target);
    }

    public static ContextExchange contextOf(final Survey survey, final Object payload, final Object context) {
        return contextOf(payload).survey(survey).context(context);
    }

    public static ContextExchange contextOf(final Object payload) {
        return new ContextExchange(payload);
    }

    public ContextExchange(final Object payload) {
        this.payload = payload;
    }

    public Object survey() {
        return survey;
    }

    public ContextExchange survey(final Survey survey) {
        this.survey = survey;
        return this;
    }

    public Object payload() {
        return payload;
    }

    public <T> Optional<T> payload(final Class<T> type) {
        return castTo(payload, type);
    }

    public UUID id() {
        return id;
    }

    public LocalDateTime dateTime() {
        return dateTime;
    }

    public Map<String, Object> metaData() {
        return metaData;
    }

    public ContextExchange put(final String key, final Object value) {
        metaData.put(key, value);
        return this;
    }

    public <T> Optional<T> get(final String key, final Class<T> type) {
        return castTo(metaData.get(key), type);
    }

    public FlowItem<?, ?> current() {
        return survey.get();
    }

    public FlowItem<?, ?> target() {
        return target;
    }

    public ContextExchange target(final FlowItem<?, ?> target) {
        this.target = target;
        return this;
    }

    public FlowItem<?, ?> flow() {
        return survey.getFirst();
    }

    public Object context() {
        return context;
    }

    public <T> Optional<T> context(final Class<T> type) {
        return castTo(context, type);
    }

    public ContextExchange context(final Object context) {
        this.context = context;
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> castTo(final Object object, final Class<T> type) {
        return Optional.ofNullable(object != null && (type.isAssignableFrom(object.getClass())) ? (T) object : null);
    }
}
