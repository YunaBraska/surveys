package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.DiagramConfig;
import berlin.yuna.survey.model.DiagramConfig.ElementType;
import berlin.yuna.survey.model.HistoryItem;
import berlin.yuna.survey.model.Route;
import berlin.yuna.survey.model.types.FlowItem;
import berlin.yuna.survey.model.types.simple.Question;
import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static berlin.yuna.survey.model.DiagramConfig.ElementType.ITEM_ANSWERED;
import static berlin.yuna.survey.model.DiagramConfig.ElementType.ITEM_CHOICE;
import static berlin.yuna.survey.model.DiagramConfig.ElementType.ITEM_CURRENT;
import static berlin.yuna.survey.model.DiagramConfig.ElementType.ITEM_DEFAULT;
import static berlin.yuna.survey.model.DiagramConfig.ElementType.ITEM_DRAFT;
import static berlin.yuna.survey.model.DiagramConfig.toKey;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static guru.nidi.graphviz.model.Link.to;
import static java.util.Objects.requireNonNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class DiagramExporter {

    private static final String SUFFIX_CHOICE = "_CHOICE";
    private DiagramConfig config = new DiagramConfig();
    //TMP used for rendering
    private final Survey survey;
    private final Set<String> links = new HashSet<>();
    private final Map<String, MutableNode> nodes = new HashMap<>();
    public static final String CONFIG_KEY_SHAPE = requireNonNull(toKey(Shape.NONE));
    public static final String CONFIG_KEY_COLOR = requireNonNull(toKey(Color.TRANSPARENT));
    public static final String CONFIG_KEY_CLASS = "x_class";
    public static final String CONFIG_KEY_SOURCE = "x_source";
    public static final String CONFIG_KEY_TARGET = "x_target";
    public static final String CONFIG_KEY_IGNORE = "x_ignore";
    public static final String CONFIG_KEY_CONDITION = "x_condition";

    public DiagramExporter(final Survey survey) {
        this.survey = survey;
    }

    /**
     * Renders a diagram from a survey flow
     *
     * @param format format of generated diagram
     * @return file path of generated diagram
     */
    public File save(final Format format) throws IOException {
        return save(null, format);
    }

    /**
     * Renders a diagram from a survey flow
     *
     * @param output nullable target path - on default generates a tmp file
     * @param format format of generated diagram
     * @return file path of generated diagram
     */
    public File save(final File output, final Format format) throws IOException {
        final File result = getFile(output, format);
        Graphviz.fromGraph(graph().directed().with(createLeaves()).graphAttr().with(Rank.dir(config.direction())))
                .width(config.width() < 1 ? (links.size() + 1) * 100 : config.width())
                .height(config.height() < 1 ? -1 : config.height())
                .render(format).toFile(result);
        return result;
    }

    public DiagramConfig config() {
        return config;
    }

    public void Config(final DiagramConfig config) {
        this.config = config;
    }

    public static boolean isChoice(final MutableNode node) {
        return Boolean.parseBoolean(String.valueOf(node.get(CONFIG_KEY_IGNORE)));
    }

    private static File getFile(final File output, final Format format) throws IOException {
        if (format == null) {
            throw new IllegalArgumentException(Format.class.getSimpleName() + " can not be null");
        }
        if (output == null) {
            return File.createTempFile("diagram_" + format.toString().toLowerCase() + "_", "." + format.fileExtension);
        }
        return output;
    }

    private MutableNode createLeaves() {
        links.clear();
        nodes.clear();
        final FlowItem<?, ?> first = survey.getFirst();
        addLeave(first, first.routes());
        return getNode(first);
    }

    private void addLeave(final FlowItem<?, ?> previous, final Set<? extends Route<?>> routes) {
        routes.forEach(route -> {
            final FlowItem<?, ?> current = route.target();
            //STOP ENDLESS CIRCULATION
            if (link(previous, route)) {
                //CHOICE
                if (current.targets().size() > 1 && config.getOrDefault(ITEM_CHOICE, CONFIG_KEY_SHAPE, Shape.NONE) != Shape.NONE) {
                    final String id = survey.getHistory().stream().filter(item -> !survey.get().match(item)).filter(item -> current.targets().stream().anyMatch(item::match)).findFirst().map(HistoryItem::getLabel).orElse(current.label() + SUFFIX_CHOICE);
                    final Question option = Question.of(current.label() + SUFFIX_CHOICE);
                    getNode(ITEM_CHOICE, option, id);
                    link(current, new Route<>(option, null, null, false));
                    addLeave(option, current.routes());
                } else {
                    addLeave(current, current.routes());
                }
            }
        });
    }

    private boolean link(final FlowItem<?, ?> first, final Route<?> route) {
        final MutableNode firstNode = getNode(first);
        final MutableNode secondNode = getNode(route.target());
        final String id = first.label() + " -> " + route.target().label();
        if (!links.contains(id)) {
            final Link link = to(secondNode).with(getLinkColor(firstNode, secondNode)).with(toLabel(route.getLabel()));
            if (!isChoice(secondNode)) {
                firstNode.addLink(link
                        .with(CONFIG_KEY_SOURCE, isChoice(firstNode) ? removeChoice(first.label()) : first.label())
                        .with(CONFIG_KEY_TARGET, route.target().label())
                        .with(CONFIG_KEY_CONDITION, getConditionName(route))
                );
            } else {
                firstNode.addLink(link);
            }
            links.add(id);
            return true;
        }
        return false;
    }

    private Label toLabel(final String label) {
        return Label.of(label == null ? "" : label);
    }

    private String getConditionName(final Route<?> route) {
        return route.hasChoice() ? route.getCondition().getClass().getSimpleName() : "";
    }

    private Color getLinkColor(final MutableNode first, final MutableNode second) {
        final String c1 = Color.named(String.valueOf(first.attrs().get(CONFIG_KEY_COLOR))).value;
        final Color c2 = Color.named(String.valueOf(second.attrs().get(CONFIG_KEY_COLOR)));
        final Color defaultColor = ((Color) config.getOrDefault(ITEM_DEFAULT, CONFIG_KEY_COLOR, Color.BLACK));
        if (!defaultColor.value.equals(c1) && !defaultColor.value.equals(c2.value)) {
            return c2;
        } else {
            return defaultColor;
        }
    }

    private MutableNode getNode(final FlowItem<?, ?> flowItem) {
        return getNode(ITEM_DEFAULT, flowItem, flowItem.label());
    }

    private MutableNode getNode(final ElementType type, final FlowItem<?, ?> flowItem, final String id) {
        return nodes.computeIfAbsent(flowItem.label(), value -> {
            final MutableNode result = mutNode(flowItem.label());
            config.get(type).stream().filter(attr -> !requireNonNull(CONFIG_KEY_COLOR).equals(toKey(attr))).forEach(result::add);
            result.add(getColorFromHistory(id));
            result.add(CONFIG_KEY_CLASS, flowItem.getClass().getSimpleName());
            result.add(CONFIG_KEY_SOURCE, type == ITEM_CHOICE ? removeChoice(flowItem.label()) : flowItem.label());
            if (type == ITEM_CHOICE) {
                result.add(CONFIG_KEY_IGNORE, true);
            }
            return result;
        });
    }

    private String removeChoice(final String label) {
        return label != null && label.endsWith(SUFFIX_CHOICE) ? label.substring(0, label.lastIndexOf(SUFFIX_CHOICE)) : label;
    }

    private Attributes<? extends ForNode> getColorFromHistory(final String label) {
        return survey.getHistory().stream()
                .filter(item -> item.getLabel().equals(label))
                .findFirst()
                .map(item -> {
                    if (item.match(survey.get())) {
                        return config.get(ITEM_CURRENT, CONFIG_KEY_COLOR);
                    } else if (item.isDraft()) {
                        return config.get(ITEM_DRAFT, CONFIG_KEY_COLOR);
                    } else if (item.isAnswered()) {
                        return config.get(ITEM_ANSWERED, CONFIG_KEY_COLOR);
                    } else {
                        return config.get(ITEM_DEFAULT, CONFIG_KEY_COLOR);
                    }
                }).orElse(config.get(ITEM_DEFAULT, CONFIG_KEY_COLOR)).orElse(Color.BLACK);
    }
}
