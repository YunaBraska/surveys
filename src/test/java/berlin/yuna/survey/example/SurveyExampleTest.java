package berlin.yuna.survey.example;

import berlin.yuna.survey.logic.Survey;
import berlin.yuna.survey.model.HistoryItemJson;
import berlin.yuna.survey.model.types.Question;
import berlin.yuna.survey.model.types.QuestionBool;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

@Tag("UnitTest")
class SurveyExampleTest {

    @Test
    void testSurvey() {
        final QuestionBool flow = QuestionBool.of("START");
        final AtomicBoolean question2BackTriggered = new AtomicBoolean(false);

        //DEFINE FLOW
        flow.target(Question.of("Q1_TRUE"), answer -> answer);
        flow.targetGet(Question.of("Q1_FALSE"), answer -> !answer)
                .targetGet(Question.of("Q2")).onBack(oldAnswer -> {
                    question2BackTriggered.set(true);
                    return true;
                })
                .targetGet(Question.of("Q3"))
                .targetGet(Question.of("END"));

        //CREATE survey that manages the history / context
        final Survey survey01 = Survey.init(flow);

        //EXECUTE survey flow
        assertThat(survey01.get(), is(equalTo(QuestionBool.of("START"))));
        assertThat(survey01.answer("Yes").get(), is(equalTo(Question.of("Q1_TRUE"))));
        assertThat(survey01.transitTo("START"), is(true));

        //TRANSITION NO BACK TRIGGERED
        assertThat(question2BackTriggered.get(), is(false));
        assertThat(survey01.get(), is(equalTo(QuestionBool.of("START"))));
        assertThat(survey01.answer("No").get(), is(equalTo(Question.of("Q1_FALSE"))));

        //EXPORT / IMPORT
        final List<HistoryItemJson> export = survey01.getHistoryJson();
        final Survey survey02 = Survey.init(flow, export);
        assertThat(export, is(equalTo(survey02.getHistoryJson())));
        assertThat(survey02.get(), is(equalTo(survey01.get())));
        assertThat(survey02.answer("next").get(), is(equalTo(Question.of("Q2"))));
        assertThat(survey02.answer("next").get(), is(equalTo(Question.of("Q3"))));
        assertThat(survey02.answer("next").get(), is(equalTo(Question.of("END"))));
        assertThat(survey02.answer("next").get(), is(equalTo(Question.of("END"))));
        assertThat(survey02.isEnded(), is(true));

        //TRANSITION BACK TRIGGERED
        assertThat(survey02.transitTo("START"), is(true));
        assertThat(question2BackTriggered.get(), is(true));
        assertThat(survey02.isEnded(), is(false));

        //TRANSITION FORWARD
        assertThat(survey02.transitTo("END"), is(true));
        assertThat(survey02.get(), is(Question.of("END")));
        assertThat(survey02.isEnded(), is(true));

        //IMPORT FINISHED FLOW
        assertThat(Survey.init(flow, survey02.getHistory()).isEnded(), is(true));
    }
}
