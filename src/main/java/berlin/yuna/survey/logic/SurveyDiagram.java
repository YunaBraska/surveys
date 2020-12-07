package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.plantuml.Identifier;
import berlin.yuna.survey.model.types.QuestionGeneric;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SkinParam;
import net.sourceforge.plantuml.UmlDiagramType;
import net.sourceforge.plantuml.activitydiagram.ActivityDiagram;
import net.sourceforge.plantuml.activitydiagram.ActivityDiagramFactory;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.DisplayPositionned;
import net.sourceforge.plantuml.cucadiagram.ILeaf;
import net.sourceforge.plantuml.cucadiagram.Ident;
import net.sourceforge.plantuml.cucadiagram.LeafType;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.LinkDecor;
import net.sourceforge.plantuml.cucadiagram.LinkType;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.VerticalAlignment;
import net.sourceforge.plantuml.style.StyleBuilder;

import java.awt.IllegalComponentStateException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.UnexpectedException;
import java.util.HashSet;
import java.util.Set;

public class SurveyDiagram {

    final Set<String> links = new HashSet<>();
    final Set<ILeaf> ends = new HashSet<>();
    //http://plantuml.com/guide
    final ActivityDiagram diagram = new ActivityDiagramFactory(null).createEmptyDiagram();
    final ILeaf start = createLeaf(LeafType.CIRCLE_START, "start");
    final StyleBuilder style = new StyleBuilder(SkinParam.create(UmlDiagramType.ACTIVITY));

    //FIXME: unused move to static render method
    public SurveyDiagram(final Survey survey) {
        start.setTop(true);
        addLeave(survey.getFirst().routes());
    }

    /**
     * Renders a diagram from a survey flow
     * @param output nullable target path - on default generates a tmp file
     * @param format format of generated diagram
     * @return file path of generated diagram
     * @throws IOException on unexpected save file issues
     */
    //FIXME: method with own unchecked exception for easier usage
    public File render(final File output, final FileFormat format) throws IOException {
        switch (format) {
            case PDF:
            case HTML:
            case MJPEG:
            case ANIMATED_GIF:
                if (!hasSVGConverter()) {
                    throw new IllegalComponentStateException("Missing dependencies see (https://plantuml.com/pdf)");
                }
                break;
            case HTML5:
            case BASE64:
                throw new IllegalComponentStateException("Missing dependency [zxing:core/2.2] see (https://stackoverflow.com/questions/19755569/error-while-converting-text-to-qr-code)");
            case SCXML:
            case PREPROC:
            case XMI_ARGO:
            case XMI_STAR:
            case XMI_STANDARD:
                throw new UnsupportedOperationException("Unsupported format [" + format + "] please contact plantuml to provide updates (https://github.com/plantuml/plantuml)");
            default:
                try (FileOutputStream os = new FileOutputStream(output, false)) {
                    //TODO: TITLE && DESCRIPTION
                    diagram.setTitle(DisplayPositionned.none(HorizontalAlignment.CENTER, VerticalAlignment.TOP));
                    diagram.exportDiagram(os, 0, new FileFormatOption(format, true));
                    if (diagram.getWarningOrError() != null && diagram.getWarningOrError().trim().length() > 1) {
                        System.err.println(diagram.getWarningOrError());
                    }
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new UnexpectedException("Something unexpected happened. First check if you have all required system libraries like 'graphviz' else read Exception", e);
                }
        }
        return output;
    }

    public static File render(final Survey survey, final FileFormat format) throws IOException {
        return render(survey, null, format);
    }

    public static File render(final Survey survey, final File output, final FileFormat format) throws IOException {
        return new SurveyDiagram(survey).render(getFile(output, format), format);
    }

    private static File getFile(final File output, final FileFormat format) throws IOException {
        if (format == null) {
            throw new IllegalArgumentException(FileFormat.class.getSimpleName() + " can not be null");
        }
        if (output == null) {
            return File.createTempFile("diagram_" + format.toString().toLowerCase() + "_", format.getFileSuffix());
        }
        return output;
    }


    private void addLeave(final Set<? extends QuestionGeneric.AnswerRoute<?>> routes) {
        addLeave(start, routes);
        connectEnds();
    }

    private void addLeave(
            final ILeaf previous,
            final Set<? extends QuestionGeneric.AnswerRoute<?>> routes
    ) {
        if (routes.isEmpty()) {
            ends.add(previous);
        }
        routes.forEach(route -> {
            final QuestionGeneric<?, ?> question = route.target();
            //CREATE LEAF
            ILeaf current = createLeaf(LeafType.ACTIVITY, question.label());
            //STOP ENDLESS CIRCULATION
            if (link(previous, current, route.getLabel())) {
                //CHOICE
                if (question.target().size() > 1) {
                    ILeaf option = createLeaf(LeafType.BRANCH, question.label() + "_CHOICE");
                    link(current, option, null);
                    current = option;
                }
                addLeave(current, question.routes());
            }
        });
    }

    private ILeaf createLeaf(final LeafType type, final String label) {
        final ILeaf leaf = diagram.getOrCreateLeaf(
                Ident.empty().add(label, null),
                new Identifier(label),
                type,
                null
        );
        leaf.setDisplay(Display.create(label));
        return leaf;
    }

    private boolean link(final ILeaf first, final ILeaf second, final String label) {
        final String id = first.getCode().getName() + " -> " + second.getCode().getName();
        if (!links.contains(id)) {
            final Link link = new Link(
                    first,
                    second,
                    new LinkType(LinkDecor.ARROW, LinkDecor.NONE),
                    label == null ? null : Display.create(label),
                    5,
                    style
            );
            diagram.addLink(link);
            links.add(id);
            return true;
        }
        return false;
    }

    private void connectEnds() {
        final ILeaf end = createLeaf(LeafType.CIRCLE_END, "end");
        ends.forEach(iLeaf -> link(iLeaf, end, null));
    }

    private boolean hasSVGConverter() {
        try {
            Class.forName("org.apache.batik.apps.rasterizer.SVGConverter");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
