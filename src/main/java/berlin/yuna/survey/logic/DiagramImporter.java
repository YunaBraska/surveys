package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.Condition;
import berlin.yuna.survey.model.exception.FlowImportException;
import berlin.yuna.survey.model.exception.FlowRuntimeException;
import berlin.yuna.survey.model.types.QuestionGeneric;
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
import java.util.Set;

import static berlin.yuna.survey.logic.DiagramExporter.CONFIG_KEY_CLASS;
import static berlin.yuna.survey.logic.DiagramExporter.CONFIG_KEY_CONDITION;
import static berlin.yuna.survey.logic.DiagramExporter.CONFIG_KEY_SOURCE;
import static berlin.yuna.survey.logic.DiagramExporter.CONFIG_KEY_TARGET;
import static berlin.yuna.survey.logic.DiagramExporter.isChoice;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class DiagramImporter {

    private final Set<Class<? extends Condition<?>>> choiceRegister = new HashSet<>();
    private final Set<Class<? extends QuestionGeneric<?, ?>>> flowRegister = new HashSet<>();

    @SuppressWarnings("unchecked")
    public DiagramImporter() {
        stream(Package.getPackages()).forEach(p -> {
            try {
                var flowItems = new Reflections(p.getName()).getSubTypesOf(QuestionGeneric.class);
                var conditionItems = new Reflections(p.getName()).getSubTypesOf(Condition.class);
                flowItems.forEach(aClass -> flowRegister.add((Class<? extends QuestionGeneric<?, ?>>) aClass));
                conditionItems.forEach(aClass -> choiceRegister.add((Class<? extends Condition<?>>) aClass));
            } catch (final Exception ignored) {
            }
        });
    }

    public QuestionGeneric<?, ?> read(final String dot) throws IOException {
        return read(new Parser().read(dot));
    }

    public QuestionGeneric<?, ?> read(final InputStream inputStream) throws IOException {
        return read(new Parser().read(inputStream));
    }

    public QuestionGeneric<?, ?> read(final File file) throws IOException {
        return read(new Parser().read(file));
    }

    public QuestionGeneric<?, ?> read(final Path path) throws IOException {
        return read(path.toFile());
    }

    public QuestionGeneric<?, ?> read(final MutableGraph graph) {
        final Map<String, QuestionGeneric<?, ?>> flowItems = toFlowItems(graph);
        graph.nodes().forEach(node -> addTargets(flowItems, node));
        return flowItems.get(graph.rootNodes().iterator().next().name().value());
    }

    private void addTargets(final Map<String, QuestionGeneric<?, ?>> flowItems, final MutableNode node) {
        node.links().forEach(link -> {
            final String source = (String) link.get(CONFIG_KEY_SOURCE);
            final String target = (String) link.get(CONFIG_KEY_TARGET);
            final QuestionGeneric<?, ?> flowItem = flowItems.get(source);
            if (source != null && target != null) {
                flowItem.target(flowItems.get(target), getConditionByName((String) link.get(CONFIG_KEY_CONDITION)));
            }
        });

    }

    public Set<Class<? extends QuestionGeneric<?, ?>>> flowRegister() {
        return flowRegister;
    }

    public Set<Class<? extends Condition<?>>> choiceRegister() {
        return choiceRegister;
    }

    private Class<? extends Condition<?>> getConditionByName(final String name) {
        return name == null || name.trim().isEmpty() ? null :
                choiceRegister.stream().filter(c -> nameEqualsClass(name, c)).findFirst().orElseThrow(() ->
                        new FlowImportException(null, null, "Condition [" + name + "] found, please register first")
                );
    }

    //TODO more matching cases like by label, replace spaces,...
    private boolean nameEqualsClass(String name, Class<? extends Condition<?>> c) {
        return c.getSimpleName().equalsIgnoreCase(name.trim().replace(" ", ""));
    }

    private Map<String, QuestionGeneric<?, ?>> toFlowItems(final MutableGraph graph) {
        return graph.nodes().stream().filter(node -> !isChoice(node)).collect(toMap(node -> (String) node.get(CONFIG_KEY_SOURCE), this::toFlowItem));
    }

    private QuestionGeneric<?, ?> toFlowItem(final MutableNode node) {
        final String type = (String) node.get(CONFIG_KEY_CLASS);
        final String label = (String) node.get(CONFIG_KEY_SOURCE);
        try {
            return flowRegister.stream().filter(clazz -> clazz.getSimpleName().equals(type)).findFirst()
                    .orElseThrow(() -> new FlowImportException(null, label, "No class registered for type [" + type + "]"))
                    .getConstructor(String.class).newInstance(label);
        } catch (NoSuchMethodException e) {
            throw new FlowRuntimeException(label, null, "Constructor not found for [" + type + "]", e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new FlowImportException(null, label, "Unable to load flowItem", e);
        }
    }
}
