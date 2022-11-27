package berlin.yuna.survey.model;

import berlin.yuna.survey.model.types.FlowItem;

import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class HistoryItem extends HistoryItemBase<Object> {

    public HistoryItem() {
        super();
    }

    public HistoryItem(final String label) {
        super(label);
    }

    public HistoryItem(final HistoryItemBase<?> item, final Object answer) {
        super(item.getLabel(), answer, item.getCreatedAt(), item.getState());
    }

    public static Optional<HistoryItem> of(final FlowItem<?, ?> flowStart, final HistoryItemBase<?> item) {
        return flowStart.get(item.getLabel()).stream().findAny().map(flowItem -> {
            if (item instanceof HistoryItem historyItem) {
                return historyItem;
            } else if (item.getAnswer() instanceof String str) {
                return new HistoryItem(item, flowItem.fromJson(str).orElse(null));
            } else {
                return null;
            }
        });
    }
}
