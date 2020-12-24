package berlin.yuna.survey.logic;


import berlin.yuna.survey.model.types.CustomCondition;
import berlin.yuna.survey.model.types.CustomCondition2;
import berlin.yuna.survey.model.types.CustomCondition4;
import berlin.yuna.survey.model.types.simple.Question;
import guru.nidi.graphviz.engine.Format;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static berlin.yuna.survey.logic.SurveyTest.createSimpleSurvey;

@Tag("IntegrationTest")
class DiagramExporterIntegrationTest {

    public static final String Q1 = "Q1";
    public static final String Q3 = "Q3";
    public static final String Q5 = "Q5";

    @Test
    void renderDiagramFromSurvey() throws IOException {
        Survey survey = createDiagramSurvey();
        for (Format format : Format.values()) {
            System.out.println(survey.diagram().save(format).toPath().toUri());
        }
        final Path exampleOutput = Path.of(System.getProperty("user.dir"), "src/test/resources/diagram_example.png");
        survey.diagram().save(exampleOutput.toFile(), Format.PNG);
    }

    public static Survey createDiagramSurvey() {
        Survey survey = createSimpleSurvey();
        survey.get(Question.of(Q1)).targetGet(survey.get(Q3), new CustomCondition());
        survey.get(Question.of(Q3)).target(Question.of(Q1), new CustomCondition2());
        survey.get(Question.of(Q3)).target(Question.of(Q5), new CustomCondition4());
        survey.answer("yes").answer("1").answer("1").transitTo("Q1");
        return survey;
    }

}