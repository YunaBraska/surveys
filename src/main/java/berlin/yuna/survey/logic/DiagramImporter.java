package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.Condition;
import berlin.yuna.survey.model.exception.FlowImportException;
import berlin.yuna.survey.model.exception.FlowRuntimeException;
import berlin.yuna.survey.model.types.FlowItem;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static berlin.yuna.survey.logic.CommonUtils.hasText;
import static berlin.yuna.survey.logic.DiagramExporter.CONFIG_KEY_CLASS;
import static berlin.yuna.survey.logic.DiagramExporter.CONFIG_KEY_CONDITION;
import static berlin.yuna.survey.logic.DiagramExporter.CONFIG_KEY_SOURCE;
import static berlin.yuna.survey.logic.DiagramExporter.CONFIG_KEY_TARGET;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * The {@link DiagramImporter} imports diagrams/flows from a {@link File} or {@link String} with a DOT format
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class DiagramImporter {

    private final Set<Class<? extends Condition<?>>> choiceRegister = new HashSet<>();
    private final Set<Class<? extends FlowItem<?, ?>>> flowRegister = new HashSet<>();

    @SuppressWarnings("unchecked")
    public DiagramImporter() {
        stream(Package.getPackages()).forEach(p -> {
            try {
                var flowItems = new Reflections(p.getName()).getSubTypesOf(FlowItem.class);
                var conditionItems = new Reflections(p.getName()).getSubTypesOf(Condition.class);
                flowItems.forEach(aClass -> flowRegister.add((Class<? extends FlowItem<?, ?>>) aClass));
                conditionItems.forEach(aClass -> choiceRegister.add((Class<? extends Condition<?>>) aClass));
            } catch (final Exception ignored) {
            }
        });
    }

    /**
     * Reads a {@link String} with a DOT format
     *
     * @param dot {@link String} with dot format
     * @return Returns imported flow
     * @throws IOException Exception on any parse error
     */
    public FlowItem<?, ?> read(final String dot) throws IOException {
        return read(new Parser().read(dot));
    }

    /**
     * Reads a {@link InputStream} with a DOT format
     *
     * @param inputStream {@link InputStream} with dot format
     * @return imported flow
     * @throws IOException Exception on any parse error
     */
    public FlowItem<?, ?> read(final InputStream inputStream) throws IOException {
        return read(new Parser().read(inputStream));
    }

    /**
     * Reads a {@link File} with a DOT format
     *
     * @param file {@link File} with dot format
     * @return imported flow
     * @throws IOException Exception on any parse error
     */
    public FlowItem<?, ?> read(final File file) throws IOException {
        return read(new Parser().read(file));
    }

    /**
     * Reads a {@link Path} with a DOT format
     *
     * @param path {@link Path} with dot format
     * @return imported flow
     * @throws IOException Exception on any parse error
     */
    public FlowItem<?, ?> read(final Path path) throws IOException {
        return read(path.toFile());
    }

    /**
     * Reads a {@link MutableGraph} with a DOT format
     *
     * @param graph {@link MutableGraph} with dot format
     * @return imported flow
     */
    public FlowItem<?, ?> read(final MutableGraph graph) {
        final Map<String, FlowItem<?, ?>> flowItems = toFlowItems(graph);
        graph.nodes().forEach(node -> addTargets(flowItems, node));
        return flowItems.get(graph.rootNodes().iterator().next().name().value());
    }

    /**
     * FlowRegister is a set of known {@link FlowItem} which are recognised and used while parsing.
     * Any missing item can lead to an error while the import.
     *
     * @return Set of known {@link FlowItem}
     */
    public Set<Class<? extends FlowItem<?, ?>>> flowRegister() {
        return flowRegister;
    }

    /**
     * ConditionRegister is a set of known {@link Condition} which are recognised and used while parsing.
     * Any missing item can lead to an error while the import.
     *
     * @return Set of known {@link Condition}
     */
    public Set<Class<? extends Condition<?>>> conditionRegister() {
        return choiceRegister;
    }

    private void addTargets(final Map<String, FlowItem<?, ?>> flowItems, final MutableNode node) {
        node.links().forEach(link -> {
            final FlowItem<?, ?> source = flowItems.get((String) link.get(CONFIG_KEY_SOURCE));
            final FlowItem<?, ?> target = flowItems.get((String) link.get(CONFIG_KEY_TARGET));
            if (source != null && target != null) {
                source.target(target, getConditionsByName(link.get(CONFIG_KEY_CONDITION)).findFirst().orElse(null));
            }
        });
    }

    private Stream<? extends Class<? extends Condition<?>>> getConditionsByName(final Object name) {
        return name == null ? Stream.empty() : stream(((String) name).split(",")).map(this::toCondition).filter(Objects::nonNull);
    }

    private Class<? extends Condition<?>> toCondition(final String name) {
        return name == null || name.trim().isEmpty() ? null :
                choiceRegister.stream().filter(c -> nameEqualsClass(name, c)).findFirst().orElseThrow(() ->
                        new FlowImportException(null, null, "Condition [" + name + "] found, please register first")
                );
    }

    //TODO more matching cases like by label, replace spaces,...
    private boolean nameEqualsClass(final String name, Class<? extends Condition<?>> c) {
        final String importName = name.trim().replace(" ", "");
        return c.getSimpleName().equalsIgnoreCase(importName)
                || c.getCanonicalName().equalsIgnoreCase(importName);
    }

    private Map<String, FlowItem<?, ?>> toFlowItems(final MutableGraph graph) {
        return graph.nodes().stream().filter(node -> hasText(node.get(CONFIG_KEY_CLASS))).collect(toMap(node -> (String) node.get(CONFIG_KEY_SOURCE), this::toFlowItem));
    }

    @SuppressWarnings("unchecked")
    private FlowItem<?, ?> toFlowItem(final MutableNode node) {
        final String type = (String) node.get(CONFIG_KEY_CLASS);
        final String label = (String) node.get(CONFIG_KEY_SOURCE);
        try {
            FlowItem<?, ?> flowItem = flowRegister.stream().filter(clazz -> clazz.getSimpleName().equals(type)).findFirst()
                    .orElseThrow(() -> new FlowImportException(null, label, "No class registered for type [" + type + "]"))
                    .getConstructor(String.class).newInstance(label);
            getConditionsByName(node.get(CONFIG_KEY_CONDITION)).forEach(flowItem::onBack);
            return flowItem;
        } catch (NoSuchMethodException e) {
            throw new FlowRuntimeException(label, null, "Constructor not found for [" + type + "]", e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new FlowImportException(null, label, "Unable to load flowItem", e);
        }
    }
}
