package berlin.yuna.survey.model;

import berlin.yuna.survey.model.types.FlowItem;

import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class HistoryItemJson extends HistoryItemBase<String> {

    public HistoryItemJson() {
        super();
    }

    public HistoryItemJson(final String label) {
        super(label);
    }

    public HistoryItemJson(final HistoryItemBase<?> item, final String answer) {
        super(item.getLabel(), answer, item.getCreatedAt(), item.getState());
    }

    public static Optional<HistoryItemJson> of(final FlowItem<?, ?> flowStart, final HistoryItemBase<?> item) {
        return flowStart.get(item.getLabel()).stream().findAny().map(flowItem -> {
            if (item instanceof HistoryItemJson historyItemJson) {
                return historyItemJson;
            } else {
                return new HistoryItemJson(item, flowItem.toJson(item.getAnswer()));
            }
        });
    }
}
