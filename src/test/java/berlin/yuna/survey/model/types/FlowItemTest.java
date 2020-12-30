package berlin.yuna.survey.model.types;


import berlin.yuna.survey.model.HistoryItem;
import berlin.yuna.survey.model.Route;
import berlin.yuna.survey.model.exception.QuestionNotFoundException;
import berlin.yuna.survey.model.exception.QuestionTypeException;
import berlin.yuna.survey.model.types.simple.Question;
import berlin.yuna.survey.model.types.simple.QuestionBool;
import berlin.yuna.survey.model.types.simple.QuestionInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
class FlowItemTest {

    public static final String Q1 = "Q1";
    public static final String Q2 = "Q2";
    public static final String Q3 = "Q3";
    public static final String Q4 = "Q4";
    public static final String Q5 = "Q5";

    public enum Q {
        Q1, Q2, Q3, Q4, Q5
    }

    @Test
    @DisplayName("Set target and get")
    void targetGetShouldReturnInput() {
        final Question flow = Question.of(Q1);
        assertThat(flow.target(Question.of(Q2)).label(), is(equalTo(Q1)));
        assertThat(flow.targetGet(Question.of(Q2)).label(), is(equalTo(Q2)));
        assertThat(flow.routes().iterator().next().target(), is(equalTo(Question.of(Q2))));

        assertThrows(QuestionNotFoundException.class, () -> flow.targetGet(null));
    }

    @Test
    @DisplayName("Set target and get with choice [COV]")
    void targetGetWithChoiceShouldReturnInput() {
        final Question flow = Question.of(Q1).target(Question.of(Q2), new CustomCondition());
        final Question flow2 = Question.of(Q3).target(Question.of(Q4), new CustomCondition());
        assertThat(flow.targets().iterator().next(), is(Question.of(Q2)));
        assertThat(flow2.targets().iterator().next(), is(Question.of(Q4)));
    }

    @Test
    @DisplayName("Get targets")
    void shouldReturnConfiguredTargets() {
        final Question subFlow = Question.of(Q2).target(Question.of(Q4)).target(Question.of(Q5), a -> a.equalsIgnoreCase("ok"));
        final Question flow = Question.of(Q1).target(subFlow).target(Question.of(Q3), a -> a.equalsIgnoreCase("fail"));

        Set<FlowItem<?, ?>> targets = flow.targets();
        assertThat(targets, hasItems(Question.of(Q2), Question.of(Q3)));
        assertThat(targets, not(hasItems(Question.of(Q1), Question.of(Q4), Question.of(Q5))));
    }

    @Test
    @DisplayName("Only one target with nullable condition allowed")
    void onlyOneTargetWithNullableConfigIsAllowed() {
        final Question flow = Question.of(Q1);
        final QuestionBool q2 = QuestionBool.of(Q2);
        final QuestionInt q3 = QuestionInt.of(Q3);

        flow.target(q2);
        assertThat(q2.parents(), hasItems(flow));
        flow.target(q3);
        assertThat(q2.parents(), not(hasItems(flow)));

        Set<FlowItem<?, ?>> targets = flow.targets();
        assertThat(targets, not(hasItems(q2)));
        assertThat(targets, hasItems(q3));
    }

    @Test
    @DisplayName("Answer")
    void answer() {
        assertThat(Question.of(Q1).answer(), is(equalTo(Optional.empty())));
        assertThat(Question.of(Q1).target(Question.of(Q2)).answer(), is(equalTo(Optional.of(Question.of(Q2)))));
    }

    @Test
    @DisplayName("Parse answer")
    void parseAnswer() {
        assertThat(QuestionBool.of(Q1).parse("yes"), is(equalTo(Optional.of(true))));
        assertThat(QuestionBool.of(Q1).parse("failed"), is(equalTo(Optional.of(false))));
        assertThat(QuestionBool.of(Q1).parse("invalid"), is(Optional.empty()));
    }

    @Test
    @DisplayName("Parse and answer")
    void parseAndAnswer() {
        QuestionBool q1 = QuestionBool.of(Q1)
                .target(Question.of(Q2), b -> b)
                .target(Question.of(Q3), b -> !b);
        assertThat(q1.parseAndAnswer("0"), is(equalTo(Optional.of(Question.of(Q3)))));
        assertThat(q1.parseAndAnswer("1"), is(equalTo(Optional.of(Question.of(Q2)))));
        assertThat(q1.parseAndAnswer("2"), is(Optional.empty()));
    }

    @Test
    @DisplayName("Return previous on creating duplication")
    void creatingQuestionTwice_returnThePreviouslyCreatedOne() {
        Question q1 = Question.of(Q1);
        assertThat(q1, is(equalTo(Question.of(Q1))));
    }

    @Test
    @DisplayName("Fail on creating with special chars")
    void creatingWithSpecialCharsInLabel_shouldFail() {
        assertThrows(IllegalArgumentException.class, () -> Question.of("My new invalid Question"));
    }

    @Test
    @DisplayName("Compare")
    void compare() {
        assertThat(Question.of(Q1).compareTo(Question.of(Q1)), is(0));
        assertThat(Question.of(Q1).compareTo(Question.of(Q2)), is(1));
        assertThat(Question.of(Q2).compareTo(Question.of(Q1)), is(-1));
        assertThat(Question.of(Q1).compareTo(QuestionInt.of(Q3)), is(2));
    }

    @Test
    @DisplayName("match SurveyAnswer")
    void match() {
        assertThat(Question.of(Q1).match(new HistoryItem("Q1", null, HistoryItem.State.DRAFT)), is(true));
        assertThat(Question.of(Q1).match(new HistoryItem("Q2", null, HistoryItem.State.DRAFT)), is(false));
    }

    @Test
    @DisplayName("on back should trigger function")
    void onBack() {
        final AtomicBoolean isBackTriggered = new AtomicBoolean(false);
        final Question flow = Question.of(Q1);
        flow.onBack(answer -> {
            isBackTriggered.set(true);
            return true;
        });
        assertThat(isBackTriggered.get(), is(false));

        flow.revert("This triggers back transition");
        assertThat(isBackTriggered.get(), is(true));
    }

    @Test
    @DisplayName("AnswerRoute [COV]")
    void checkAnswerRoute() {
        final CustomCondition customChoice = new CustomCondition();
        Route<String> route = new Route<>(Question.of(Q1), null, customChoice, false);
        assertThat(route.target(), is(Question.of(Q1)));
        assertThat(route.getLabel(), is(customChoice.getLabel()));
        assertThat(route.equals(new Route<>(Question.of(Q1), null, customChoice, false)), is(true));
        assertThat(route.toString(), is(containsString("AnswerRoute{target=Question{label='Q1'}")));
    }

    @Test
    void diagram() {
        assertThat(Question.of(Q1).diagram(), is(notNullValue()));
    }

    @Test
    @DisplayName("Get by enum, string and type [COV]")
    void Get() {
        final Question q2 = Question.of(Q2);
        final Question flow = Question.of(Q1).target(q2.target(Question.of(Q3)));
        //By String
        String qString = null;
        assertThat(flow.get(Q1).get().targets(), hasItems(q2));
        assertThat(flow.getOrElse(Q2, Question.of(Q3)), is(q2));
        assertThat(flow.getOrElse(Q5, q2), is(q2));
        assertThat(flow.getOrElse(qString, q2), is(q2));
        //By Enum
        Enum qEnum = null;
        assertThat(flow.get(Q.Q1).get().targets(), hasItems(q2));
        assertThat(flow.getOrElse(Q.Q2, Question.of(Q3)), is(q2));
        assertThat(flow.getOrElse(Q.Q5, q2), is(q2));
        assertThat(flow.getOrElse(qEnum, q2), is(q2));
        //By Type
        Question qType = null;
        assertThat(flow.get(Question.of(Q1)).get().targets(), hasItems(q2));
        assertThat(flow.getOrElse(Question.of(Q2), Question.of(Q3)), is(q2));
        assertThat(flow.getOrElse(Question.of(Q5), q2), is(q2));
        assertThat(flow.getOrElse(qType, q2), is(q2));
        //By wrong type
        assertThrows(QuestionTypeException.class, () -> flow.get(QuestionBool.of(Q2)));
    }

    @Test
    @DisplayName("Equals [COV]")
    void equals() {
        assertThat(Question.of(Q1).equals(Question.of(Q1)), is(true));
        assertThat(Question.of(Q1).equals(Question.of(Q2)), is(false));
    }

    @Test
    @DisplayName("ToString [COV]")
    void toStringTest() {
        assertThat(Question.of(Q1).toString(), is(equalTo("Question{label='Q1'}")));
    }
}