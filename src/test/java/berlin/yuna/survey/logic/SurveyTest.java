package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.SurveyAnswer;
import berlin.yuna.survey.model.types.QuestionGeneric;
import berlin.yuna.survey.model.types.simple.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @BeforeEach
    void setUp() {
        Question.clearAll();
    }

    @Test
    @DisplayName("Init with enum")
    void initWithEnum() {
        assertThrows(IllegalArgumentException.class, () -> Survey.init(Q.Q1), "Missing QuestionGeneric given was null");
        final Question q1 = Question.of(Q1);
        final Survey survey = Survey.init(Q.Q1);
        assertThat(survey, is(notNullValue()));
        assertThat(survey.get(), is(q1));
    }

    @Test
    @DisplayName("Init with String")
    void initWithString() {
        assertThrows(IllegalArgumentException.class, () -> Survey.init(Q1), "Missing QuestionGeneric given was null");
        final Question q1 = Question.of(Q.Q1);
        final Survey survey = Survey.init(Q1);
        assertThat(survey, is(notNullValue()));
        assertThat(survey.get(), is(q1));
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
        Enum<?> e = null;
        Question q = null;
        String s = null;
        assertThrows(IllegalArgumentException.class, () -> Survey.init(e));
        assertThrows(IllegalArgumentException.class, () -> Survey.init(q));
        assertThrows(IllegalArgumentException.class, () -> Survey.init(s));
    }

    @Test
    @DisplayName("Init from history without answer")
    void initFlowFromHistoryWithoutAnswer() {
        final Survey survey = Survey.init(Question.of(START));
        assertThat(survey.get(), is(Question.of(START)));

        final Survey reloaded = Survey.init(survey.getHistory());
        assertThat(reloaded.get(), is(Question.of(START)));
    }

    @Test
    @DisplayName("Init from empty history")
    void intFlowFromEmptyHistory() {
        assertThrows(IllegalArgumentException.class, () -> Survey.init(new HashSet<>()), "Missing QuestionGeneric, given was null");
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

        final Survey surveyCopy = Survey.init(survey.getHistory());
        assertThat(surveyCopy.get(), is(equalTo(survey.get())));
        assertThat(surveyCopy.getPrevious(), is(equalTo(survey.getPrevious())));
    }

    @Test
    @DisplayName("Get")
    void getShouldReturnCurrentQuestion() {
        final Survey survey = createSimpleSurvey();
        Question.get(START).target(Question.get(Q3), answer -> answer.equals("1"));

        assertThat(survey.get(), equalTo(Question.get(START)));
        survey.answer("1");
        assertThat(survey.get(), equalTo(Question.get(Q3)));
    }

    @Test
    @DisplayName("First")
    void firstShouldReturnFirstQuestion() {
        final Survey survey = createSimpleSurvey();
        survey.answer("1").answer("2").answer("3");
        assertThat(survey.getFirst(), is(Question.get(START)));
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
        Question.get(Q1).target(Question.get(Q3), answer -> answer.equals("1"));

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
        Question.of(Q2).onBack(answer -> backTriggered.set(true));

        //BACKWARD
        QuestionGeneric<?, ?> before = survey.get();
        survey.transitTo(Question.of(Q1));

        assertThat(survey.get(), is(equalTo(Question.of(Q1))));
        assertThat(survey.getPrevious(), is(equalTo(Question.of(START))));
        assertThat(backTriggered.get(), is(true));
        assertThat(survey.getHistory().stream().filter(SurveyAnswer::isDraft).toArray().length, is(2));
        assertThat(survey.getHistory().stream().filter(SurveyAnswer::isNotDraft).toArray().length, is(1));

        //FORWARD
        survey.transitTo(before.label());
        assertThat(survey.get(), is(equalTo(before)));
        assertThat(survey.getPrevious(), is(equalTo(Question.of(Q2))));
        assertThat(survey.getHistory().stream().filter(SurveyAnswer::isDraft).toArray().length, is(0));
        assertThat(survey.getHistory().stream().filter(SurveyAnswer::isNotDraft).toArray().length, is(3));
    }

    @Test
    @DisplayName("Get previous")
    void getPrevious() {
        Question.of(START)
                .target(Question.of(Q2).target(Question.of(Q4)), answer -> answer.equals("yes"))
                .target(Question.of(Q3).target(Question.of(Q4)), answer -> answer.equals("no"));
        Question.of(Q4).target(Question.of(END));
        assertThat(Survey.init(Question.of(START)).getPrevious(), is(nullValue()));

        final Survey flowYes = Survey.init(Question.of(START)).answer("yes");
        assertThat(flowYes.get(), is(equalTo(Question.of(Q2))));
        assertThat(flowYes.answer(Q4).getPrevious(), is(equalTo(Question.of(Q2))));

        final Survey flowNo = Survey.init(Question.of(START)).answer("no");
        assertThat(flowNo.get(), is(equalTo(Question.of(Q3))));
        assertThat(flowNo.answer(Q4).getPrevious(), is(equalTo(Question.of(Q3))));
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