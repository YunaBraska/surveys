package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.exception.FlowImportException;
import berlin.yuna.survey.model.exception.FlowRuntimeException;
import berlin.yuna.survey.helper.CustomCondition4;
import berlin.yuna.survey.helper.CustomConditionInvalid;
import berlin.yuna.survey.helper.QuestionInvalid;
import berlin.yuna.survey.model.types.Question;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import static berlin.yuna.survey.logic.DiagramExporterIntegrationTest.Q3;
import static berlin.yuna.survey.logic.DiagramExporterIntegrationTest.createDiagramSurvey;
import static berlin.yuna.survey.model.DiagramConfig.ElementType.ITEM_CHOICE;
import static java.io.File.createTempFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Tag("UnitTest")
class DiagramImporterTest {

    @Test
    @DisplayName("Export / Import with choice")
    void importDiagramWithChoice() throws IOException {
        Survey survey = createDiagramSurvey();
        final DiagramExporter diagramExporter = survey.diagram();
        final DiagramImporter flowImporter = new DiagramImporter();
        for (int i = 0; i < 4; i++) {
            final File exported = diagramExporter.save(createTempFile("diagram_" + i + "_", "." + Format.DOT.fileExtension), Format.DOT);
            diagramExporter.save(createTempFile("diagram_" + i + "_", "." + Format.PNG.fileExtension), Format.PNG);
            survey = validateAndReturn(survey, exported, Survey.init(flowImporter.read(exported)));
        }
    }

    @Test
    @DisplayName("Export / Import without choice")
    void importDiagramWithoutChoice() throws IOException {
        Survey survey = createDiagramSurvey();
        final DiagramExporter diagramExporter = survey.diagram().config().add(ITEM_CHOICE, Shape.NONE).diagram();
        final DiagramImporter flowImporter = new DiagramImporter();
        for (int i = 0; i < 4; i++) {
            final File exported = diagramExporter.save(createTempFile("diagram_" + i + "_", "." + Format.DOT.fileExtension), Format.DOT);
            diagramExporter.save(createTempFile("diagram_" + i + "_", "." + Format.PNG.fileExtension), Format.PNG);
            survey = validateAndReturn(survey, exported, Survey.init(flowImporter.read(exported)));
        }
    }

    @Test
    @DisplayName("Export / Import with back transitions")
    void importDiagramWithBackTransitions() throws IOException {
        Survey survey = createDiagramSurvey();
        final DiagramExporter diagramExporter = survey.diagram().config().showBackTransition(true).diagram();
        final DiagramImporter flowImporter = new DiagramImporter();
        for (int i = 0; i < 4; i++) {
            final File exported = diagramExporter.save(createTempFile("diagram_" + i + "_", "." + Format.DOT.fileExtension), Format.DOT);
            diagramExporter.save(createTempFile("diagram_" + i + "_", "." + Format.PNG.fileExtension), Format.PNG);
            survey = validateAndReturn(survey, exported, Survey.init(flowImporter.read(exported)));
        }
    }

    @Test
    @DisplayName("Import with different types")
    void importDifferentTypes() throws IOException {
        final Survey survey = createDiagramSurvey();
        final File exported = survey.diagram().save(Format.DOT);
        assertThat(survey.getFirst(), is(equalTo(new DiagramImporter().read(exported))));
        assertThat(survey.getFirst(), is(equalTo(new DiagramImporter().read(exported.toPath()))));
        assertThat(survey.getFirst(), is(equalTo(new DiagramImporter().read(Files.readString(exported.toPath())))));
        assertThat(survey.getFirst(), is(equalTo(new DiagramImporter().read(new FileInputStream(exported)))));
    }

    @Test
    @DisplayName("Import with unknown condition")
    void unknownCondition() throws IOException {
        final Survey survey = createDiagramSurvey();
        final String exported = Files.readString(survey.diagram().save(Format.DOT).toPath());
        final String modifiedExport = exported.replace(CustomCondition4.class.getSimpleName(), "UnknownCondition");
        assertThrows(FlowImportException.class, () -> new DiagramImporter().read(modifiedExport));
    }

    @Test
    @DisplayName("Import with invalid Condition")
    void invalidCondition() throws IOException {
        final Survey survey = createDiagramSurvey();
        final String exported = Files.readString(survey.diagram().save(Format.DOT).toPath());
        final String modifiedExport = exported.replace(CustomCondition4.class.getSimpleName(), CustomConditionInvalid.class.getSimpleName());
        assertThrows(FlowRuntimeException.class, () -> new DiagramImporter().read(modifiedExport));
    }

    @Test
    @DisplayName("Import with invalid flowItem")
    void invalidFlowItem() throws IOException {
        final Survey survey = createDiagramSurvey();
        final String exported = Files.readString(survey.diagram().save(Format.DOT).toPath());
        final String modifiedExport = exported.replace(Question.class.getSimpleName(), QuestionInvalid.class.getSimpleName());
        assertThrows(FlowRuntimeException.class, () -> new DiagramImporter().read(modifiedExport));
    }

    @Test
    @DisplayName("FlowImporter Register")
    void registerCheck() {
        assertThat(new DiagramImporter().flowRegister(), is(not(empty())));
        assertThat(new DiagramImporter().conditionRegister(), is(not(empty())));
    }

    private Survey validateAndReturn(final Survey survey, final File exported, final Survey imported) {
        assertThat(exported.exists(), is(true));
        assertThat((int) exported.length(), is(greaterThan(0)));
        assertThat(imported.getFirst(), is(equalTo(survey.getFirst())));
        assertThat(imported.get(Q3), is(equalTo(survey.get(Q3))));
        assertThat(imported.get(Q3).transitions().size(), is(equalTo(survey.get(Q3).transitions().size())));
        return imported;
    }
}