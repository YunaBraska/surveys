package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.HistoryItem;
import berlin.yuna.survey.model.exception.QuestionNotFoundException;
import berlin.yuna.survey.model.types.QuestionGeneric;
import berlin.yuna.survey.model.types.simple.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
class SurveyTest {

    //TODO: TestMap
    //  Group
    //      Questions[]
    //          Question (OnBack, OnResult)
    //  Routes[]
    //      Question -> finish
    //      Question -> back(action)
    //      Question -> Group (does all back actions in between)

    //TODO on group transition
    //TODO answer multiple questions
    //TODO add sub questions (block adding questions when sub-questions are set and other way around)
    //TODO allow all transitions and block manually or other way round

    public static final String Q1 = "Q1";
    public static final String Q2 = "Q2";
    public static final String Q3 = "Q3";
    public static final String Q4 = "Q4";
    public static final String START = "START";
    public static final String END = "END";

    public enum Q {
        Q1, Q2, Q3, Q4, Q5
    }

    @Test
    @DisplayName("Init with Question")
    void initWithQuestion() {
        final Survey survey = Survey.init(Question.of(Q.Q1));
        assertThat(survey, is(notNullValue()));
        assertThat(survey.get(), is(Question.of(Q.Q1)));
    }

    @Test
    @DisplayName("Init with null")
    void initWithNull() {
        assertThrows(IllegalArgumentException.class, () -> Survey.init(null));
    }

    @Test
    @DisplayName("Init from history without answer")
    void initFlowFromHistoryWithoutAnswer() {
        final Question flow = Question.of(START);
        final Survey survey = Survey.init(flow);
        assertThat(survey.get(), is(flow));

        final Survey reloaded = Survey.init(flow, survey.getHistory());
        assertThat(reloaded.get(), is(flow));
    }

    @Test
    @DisplayName("Init from empty history")
    void intFlowFromEmptyHistory() {
        final Survey survey = Survey.init(Question.of(Q.Q1), new HashSet<>());
        assertThat(survey, is(notNullValue()));
        assertThat(survey.get(), is(Question.of(Q.Q1)));
    }

    //TODO: from finished history?
    @Test
    @DisplayName("Init from history")
    void intFlowFromHistory() {
        final Survey survey = createSimpleSurvey();

        survey.answer("something");
        assertThat(survey.get(), is(notNullValue()));
        assertThat(survey.getPrevious(), is(notNullValue()));
        assertThat(survey.get(), is(not(equalTo(survey.getPrevious()))));

        final Survey surveyCopy = Survey.init(survey.getFirst(), survey.getHistory());
        assertThat(surveyCopy.get(), is(equalTo(survey.get())));
        assertThat(surveyCopy.getPrevious(), is(equalTo(survey.getPrevious())));
    }

    @Test
    @DisplayName("Get")
    void getShouldReturnCurrentQuestion() {
        final Survey survey = createSimpleSurvey();
        survey.getFirst().target(Question.of(Q3), answer -> answer.equals("1"));

        assertThat(survey.get(), equalTo(Question.of(START)));
        survey.answer("1");
        assertThat(survey.get(), equalTo(Question.of(Q3)));
    }

    @Test
    @DisplayName("First")
    void firstShouldReturnFirstQuestion() {
        final Survey survey = createSimpleSurvey();
        survey.answer("1").answer("2").answer("3");
        assertThat(survey.getFirst(), is(Question.of(START)));
    }

    @Test
    @DisplayName("Ended")
    void surveyShouldEndOnLastQuestion() {
        final Survey survey = Survey.init(Question.of(Q1).target(Question.of(Q2)));
        survey.answer("1");
        assertThat(survey.isEnded(), is(false));
        survey.answer("1");
        assertThat(survey.isEnded(), is(true));
    }

    @Test
    @DisplayName("History")
    void historyShouldReturnAllGivenAnswers() {
        final Survey survey = createSimpleSurvey();
        survey.answer("1").answer("2").answer("3");
        assertThat(survey.getHistory(), hasSize(4));
    }

    @Test
    @DisplayName("Simple forward flow with answer")
    void flowForwardShouldMoveToTargetOnAnswer() {
        final Survey survey = createSimpleSurvey();
        survey.get(Q1).target(survey.get(Q3), answer -> answer.equals("1"));

        final AtomicInteger count = new AtomicInteger(1);
        while (!survey.isEnded()) {
            survey.answer("1");
            assertThat(survey.get().label(), is(not(equalTo(Q2))));
            assertThat(count.getAndIncrement(), is(lessThanOrEqualTo(4)));
            assertThat(survey.getHistorySize(), is(lessThanOrEqualTo(count.get())));
        }
        System.out.println(survey.getDurationsMS());
    }

    //TODO: transition back is blocked
    //TODO: transition forward has not enough answers
    //TODO: transition not possible (Question does not belong to flow)
    //TODO: circular flow (transition back on target)
    @Test
    @DisplayName("Transition back and forward")
    void transitionBackAndForward() {
        final AtomicBoolean backTriggered = new AtomicBoolean(false);
        final Survey survey = createSimpleSurvey().answer(START).answer(Q1).answer(Q2);
        survey.get(Q2).onBack(answer -> backTriggered.set(true));

        //INVALID TARGET
        assertThrows(QuestionNotFoundException.class, () -> survey.transitTo(Question.of(Q4)));
        QuestionGeneric<?, ?> before = survey.get();
        //SAME TARGET
        survey.transitTo(survey.get());
        assertThat(survey.get(), is(equalTo(before)));

        //BACKWARD
        survey.transitTo(Question.of(Q1));
        assertThat(survey.get(), is(equalTo(Question.of(Q1))));
        assertThat(survey.getPrevious(), is(equalTo(Question.of(START))));
        assertThat(backTriggered.get(), is(true));
        assertThat(survey.getHistory().stream().filter(HistoryItem::isDraft).toArray().length, is(2));
        assertThat(survey.getHistory().stream().filter(HistoryItem::isNotDraft).toArray().length, is(1));

        //FORWARD
        survey.transitTo(before.label());
        assertThat(survey.get(), is(equalTo(before)));
        assertThat(survey.getPrevious(), is(equalTo(Question.of(Q2))));
        assertThat(survey.getHistory().stream().filter(HistoryItem::isDraft).toArray().length, is(0));
        assertThat(survey.getHistory().stream().filter(HistoryItem::isNotDraft).toArray().length, is(3));
    }

    @Test
    @DisplayName("Get previous")
    void getPrevious() {
        final Question flow = Question.of(START);
        final Question endFlow = Question.of(END);
        flow.target(Question.of(Q2).target(endFlow), answer -> answer.equals("yes"));
        flow.target(Question.of(Q3).target(endFlow), answer -> answer.equals("no"));

        assertThat(Survey.init(flow).getPrevious(), is(nullValue()));
        final Survey flowYes = Survey.init(flow).answer("yes");
        assertThat(flowYes.get(), is(equalTo(Question.of(Q2))));
        assertThat(flowYes.answer(Q4).getPrevious(), is(equalTo(Question.of(Q2))));

        final Survey flowNo = Survey.init(flow).answer("no");
        assertThat(flowNo.get(), is(equalTo(Question.of(Q3))));
        assertThat(flowNo.answer(Q4).getPrevious(), is(equalTo(Question.of(Q3))));
    }

    @Test
    @DisplayName("Get [COV]")
    void get() {
        final Survey survey = Survey.init(Question.of(START).target(Question.of(Q1).target(Question.of(Q2))));
        assertThat(survey.get(), is(equalTo(Question.of(START))));
        assertThat(survey.get(Q1), is(equalTo(Question.of(Q1))));
        assertThat(survey.get(Q.Q1), is(equalTo(Question.of(Q1))));
        assertThat(survey.get(Question.of(Q1)), is(equalTo(Question.of(Q1))));
        assertThat(survey.get(Q.Q3), is(nullValue()));

    }

    public static Survey createSimpleSurvey() {
        Question start = Question.of("START");
        start.targetGet(Question.of("Q1"))
                .targetGet(Question.of("Q2"))
                .targetGet(Question.of("Q3"))
                .targetGet(Question.of("END"));
        return Survey.init(start);
    }

}