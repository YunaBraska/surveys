package berlin.yuna.survey.logic;


import berlin.yuna.survey.helper.CustomCondition;
import berlin.yuna.survey.helper.CustomCondition2;
import berlin.yuna.survey.helper.CustomCondition4;
import berlin.yuna.survey.model.types.FlowItem;
import berlin.yuna.survey.model.types.Question;
import guru.nidi.graphviz.engine.Format;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static berlin.yuna.survey.logic.SurveyTest.createSimpleSurvey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
class DiagramExporterIntegrationTest {

    public static final String Q1 = "Q1";
    public static final String Q3 = "Q3";
    public static final String Q5 = "Q5";

    @Test
    @DisplayName("Render from survey")
    void renderDiagramFromSurvey() throws IOException {
        final Survey survey = createDiagramSurvey();
        assertThat(survey.diagram().config().diagram().survey(), is(equalTo(survey)));
        for (Format format : Format.values()) {
            System.out.println(survey.diagram().save(format).toPath().toUri());
        }
        final Path exampleOutput = Path.of(System.getProperty("user.dir"), "src/test/resources/diagram_example.svg");
        survey.diagram().save(exampleOutput.toFile(), Format.SVG);
    }

    @Test
    @DisplayName("Render from flowItem")
    void renderDiagramFromFlowItem() throws IOException {
        final FlowItem<?, ?> flow = createDiagramSurvey().getFirst();
        assertThat(flow.diagram().config().diagram().survey().getFirst(), is(equalTo(flow)));
        assertThat(flow.diagram().save(Format.SVG).exists(), is(true));
    }

    @Test
    @DisplayName("Save diagram without format")
    void saveDiagramWithoutFormat_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> Question.of("Q1").diagram().save(null, null));
    }

    @Test
    @DisplayName("Set config")
    void setConfig() {
        final DiagramExporter diagramExporter = Question.of("Q1").diagram();
        diagramExporter.Config(null);
        assertThat(diagramExporter.config(), is(nullValue()));
    }

    public static Survey createDiagramSurvey() {
        final Survey survey = createSimpleSurvey();
        survey.get(Question.of(Q1)).targetGet(survey.get(Q3), new CustomCondition());
        survey.get(Question.of(Q3)).target(Question.of(Q1), new CustomCondition2());
        survey.get(Question.of(Q3)).target(Question.of(Q5), new CustomCondition4()).onBack(new CustomCondition4(), new CustomCondition2());
        assertThat(survey.answer("yes").answer("1").answer("4").transitTo("Q1"), is(true));
        return survey;
    }

}
