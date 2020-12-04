package berlin.yuna.survey.logic;


import berlin.yuna.survey.model.types.CustomChoice;
import berlin.yuna.survey.model.types.simple.Question;
import net.sourceforge.plantuml.FileFormat;
import org.junit.jupiter.api.Test;

import java.awt.IllegalComponentStateException;
import java.io.IOException;

import static berlin.yuna.survey.logic.SurveyTest.createSimpleSurvey;

class SurveyDiagramTest {

    public static final String Q1 = "Q1";
    public static final String Q3 = "Q3";
    public static final String Q5 = "Q5";

    @Test
    void renderDiagramFromSurvey() throws IOException {
        Survey survey = createSimpleSurvey();
        Question.of(Q1).targetGet(Question.of(Q3), new CustomChoice());
        Question.of(Q3).target(Question.of(Q1), answer -> answer.equals("2"));
        Question.of(Q3).target(Question.of(Q5), answer -> answer.equals("4"));
        for (FileFormat format : FileFormat.values()) {
            try {
                System.out.println(SurveyDiagram.render(survey, null, format).toPath().toUri());
            } catch (Exception e) {
                if (!(e instanceof UnsupportedOperationException) && !(e instanceof IllegalComponentStateException)) {
                    System.out.println("Format " + format);
                    throw e;
                }
            }
        }
    }

}