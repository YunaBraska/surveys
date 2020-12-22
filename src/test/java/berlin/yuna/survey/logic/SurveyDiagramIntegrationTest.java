package berlin.yuna.survey.logic;


import berlin.yuna.survey.model.types.CustomCondition;
import berlin.yuna.survey.model.types.simple.Question;
import guru.nidi.graphviz.engine.Format;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static berlin.yuna.survey.logic.SurveyTest.createSimpleSurvey;

@Tag("IntegrationTest")
class SurveyDiagramIntegrationTest {

    public static final String Q1 = "Q1";
    public static final String Q3 = "Q3";
    public static final String Q5 = "Q5";

    @Test
    void renderDiagramFromSurvey() throws IOException {
        Survey survey = createSimpleSurvey();
        survey.get(Question.of(Q1)).targetGet(survey.get(Q3), new CustomCondition());
        survey.get(Question.of(Q3)).target(Question.of(Q1), answer -> answer.equals("2"));
        survey.get(Question.of(Q3)).target(Question.of(Q5), answer -> answer.equals("4"));
        survey.answer("yes").answer("1").answer("1").transitTo("Q1");
        for (Format format : Format.values()) {
            System.out.println(new SurveyDiagram(survey).render(format).toPath().toUri());
        }
        final Path exampleOutput = Path.of(System.getProperty("user.dir"), "src/test/resources/diagram_example.png");
        new SurveyDiagram(survey).render(exampleOutput.toFile(), Format.PNG);
    }

}