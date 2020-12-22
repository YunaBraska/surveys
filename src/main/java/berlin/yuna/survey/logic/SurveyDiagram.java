package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.HistoryItem;
import berlin.yuna.survey.model.Route;
import berlin.yuna.survey.model.types.QuestionGeneric;
import guru.nidi.graphviz.attribute.Color;
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

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.mutNode;
import static guru.nidi.graphviz.model.Link.to;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class SurveyDiagram {

    //CONFIGS
    private Color colorDraft = Color.BLUE;
    private Color colorDefault = Color.BLACK;
    private Color colorAnswered = Color.GREEN;
    private Color colorCurrent = Color.ORANGE;
    private Shape shapeChoice = Shape.OVAL;
    private Shape shapeDefault = Shape.RECTANGLE;
    private Rank.RankDir direction = Rank.RankDir.LEFT_TO_RIGHT;
    private int width = -1;
    private int height = -1;

    //TMP used for rendering
    private final Survey survey;
    private final Set<String> links = new HashSet<>();
    private final Map<String, MutableNode> nodes = new HashMap<>();

    public SurveyDiagram(final Survey survey) {
        this.survey = survey;
    }

    /**
     * Renders a diagram from a survey flow
     *
     * @param format format of generated diagram
     * @return file path of generated diagram
     */
    public File render(final Format format) throws IOException {
        return render(null, format);
    }

    /**
     * Renders a diagram from a survey flow
     *
     * @param output nullable target path - on default generates a tmp file
     * @param format format of generated diagram
     * @return file path of generated diagram
     */
    public File render(final File output, final Format format) throws IOException {
        final File result = getFile(output, format);
        final Graphviz graph = Graphviz.fromGraph(graph().directed().with(createLeaves()).graphAttr().with(Rank.dir(direction)));
        graph.width(width < 1 ? (links.size() +1) * 100 : width).height(height < 1 ? -1 : height).render(format).toFile(result);
        return result;
    }

    public SurveyDiagram colorDefault(final Color colorDefault) {
        this.colorDefault = colorDefault;
        return this;
    }

    public SurveyDiagram colorAnswered(final Color colorAnswered) {
        this.colorAnswered = colorAnswered;
        return this;
    }

    public SurveyDiagram colorDraft(final Color colorDraft) {
        this.colorDraft = colorDraft;
        return this;
    }

    public SurveyDiagram colorCurrent(final Color colorCurrent) {
        this.colorCurrent = colorCurrent;
        return this;
    }

    public SurveyDiagram direction(final Rank.RankDir direction) {
        this.direction = direction;
        return this;
    }

    public SurveyDiagram shapeChoice(Shape shapeChoice) {
        this.shapeChoice = shapeChoice;
        return this;
    }

    public SurveyDiagram shapeDefault(Shape shapeDefault) {
        this.shapeDefault = shapeDefault;
        return this;
    }

    public SurveyDiagram width(int width) {
        this.width = width;
        return this;
    }

    public SurveyDiagram height(int height) {
        this.height = height;
        return this;
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
        final QuestionGeneric<?, ?> first = survey.getFirst();
        addLeave(getNode(shapeDefault, first.label()), first.routes());
        return getNode(shapeDefault, first.label());
    }

    private void addLeave(final MutableNode previous, final Set<? extends Route<?>> routes) {
        routes.forEach(route -> {
            final QuestionGeneric<?, ?> question = route.target();
            final MutableNode current = getNode(shapeDefault, question.label());
            //STOP ENDLESS CIRCULATION
            if (link(previous, current, route.getLabel())) {
                //CHOICE
                if (shapeChoice != Shape.NONE && question.targets().size() > 1) {
                    final String id = survey.getHistory().stream().filter(item -> !survey.get().match(item)).filter(item -> question.targets().stream().anyMatch(item::match)).findFirst().map(HistoryItem::getLabel).orElse(question.label() + "_CHOICE");
                    MutableNode option = getNode(shapeChoice, question.label() + "_CHOICE", id);
                    link(current, option, null);
                    addLeave(option, question.routes());
                } else {
                    addLeave(current, question.routes());
                }
            }
        });
    }

    private Color getColor(final String label) {
        return survey.getHistory().stream()
                .filter(item -> item.getLabel().equals(label))
                .findFirst()
                .map(item -> {
                    if (item.match(survey.get())) {
                        return colorCurrent;
                    } else if (item.isDraft()) {
                        return colorDraft;
                    } else if (item.isAnswered()) {
                        return colorAnswered;
                    } else {
                        return colorDefault;
                    }
                }).orElse(colorDefault);
    }

    private boolean link(final MutableNode first, final MutableNode second, final String label) {
        final String id = first.name().value() + " -> " + second.name().value();
        if (!links.contains(id)) {
            final Link link = to(second)
                    .with(getLinkColor(first, second))
                    .with(Label.of(label == null ? "" : label));
            first.addLink(link);
            links.add(id);
            return true;
        }
        return false;
    }

    private Color getLinkColor(final MutableNode first, final MutableNode second) {
        final Color c1 = Color.named(String.valueOf(first.attrs().get("color")));
        final Color c2 = Color.named(String.valueOf(second.attrs().get("color")));
        if (!c1.value.equals(colorDefault.value) && !c2.value.equals(colorDefault.value)) {
            return c2;
        } else {
            return colorDefault;
        }
    }

    private MutableNode getNode(final Shape shape, final String label) {
        return getNode(shape, label, label);
    }

    private MutableNode getNode(final Shape shape, final String label, final String id) {
        return nodes.computeIfAbsent(label, value -> {
            MutableNode result = mutNode(label);
            result.add(getColor(id));
            result.add(shape);
            return result;
        });
    }
}
