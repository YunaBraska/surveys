package berlin.yuna.survey.model.types;


import berlin.yuna.survey.model.HistoryItem;
import berlin.yuna.survey.model.types.simple.Question;
import berlin.yuna.survey.model.types.simple.QuestionBool;
import berlin.yuna.survey.model.types.simple.QuestionInt;
import org.junit.jupiter.api.BeforeEach;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
class QuestionGenericTest {

    public static final String Q1 = "Q1";
    public static final String Q2 = "Q2";
    public static final String Q3 = "Q3";
    public static final String Q4 = "Q4";
    public static final String Q5 = "Q5";

    public enum Q {
        Q1, Q2, Q3, Q4, Q5
    }

    @BeforeEach
    void setUp() {
        Question.clearAll();
    }

    @Test
    void circularlyTest() {
        new QuestionBool(Q1).target(Question.of(Q3)).targetGet(Question.of(Q2)).target(new QuestionInt(Q1));
    }

    @Test
    @DisplayName("Delete cache, routes and config")
    void deleteCacheAndRouted() {
        assertThat(Question.of(Q1), is(equalTo(Question.get(Q.Q1))));
        Question.clearAll();
        assertThat(Question.get(Q1), is(nullValue()));
    }

    @Test
    @DisplayName("Exists")
    void checkIfQuestionExists() {
        assertThat(Question.exists(Q1), is(false));
        Question.of(Q1);
        assertThat(Question.exists(Q1), is(true));
        assertThat(Question.exists(Q.Q1), is(true));
    }

    @Test
    @DisplayName("Set target and get")
    void targetGetShouldReturnInput() {
        assertThat(Question.of(Q1).target(Question.of(Q2)).label(), is(equalTo(Q1)));
        assertThat(Question.of(Q1).targetGet(Question.of(Q2)).label(), is(equalTo(Q2)));
        assertThat(Question.of(Q1).routes().iterator().next().target(), is(equalTo(Question.of(Q2))));
    }

    @Test
    @DisplayName("Set target and get with choice [COV]")
    void targetGetWithChoiceShouldReturnInput() {
        Question.of(Q1).targetGet(Question.of(Q2), new CustomCondition());
        Question.of(Q3).target(Question.of(Q4), new CustomCondition());
        assertThat(Question.of(Q1).targets().iterator().next(), is(Question.of(Q2)));
        assertThat(Question.of(Q3).targets().iterator().next(), is(Question.of(Q4)));
    }

    @Test
    @DisplayName("Get targets")
    void shouldReturnConfiguredTargets() {
        Question.of(Q1).target(Question.of(Q2)).target(Question.of(Q3), a -> a.equalsIgnoreCase("fail"));
        Question.of(Q2).target(Question.of(Q4)).target(Question.of(Q5), a -> a.equalsIgnoreCase("ok"));

        Set<QuestionGeneric<?, ?>> targets = Question.get(Q1).targets();
        assertThat(targets, hasItems(Question.get(Q2), Question.get(Q3)));
        assertThat(targets, not(hasItems(Question.get(Q1), Question.get(Q4), Question.get(Q5))));
    }

    @Test
    @DisplayName("Only one target with nullable condition allowed")
    void onlyOneTargetWithNullableConfigIsAllowed() {
        Question.of(Q1).target(QuestionBool.of(Q2));
        Question.of(Q1).target(QuestionInt.of(Q3));

        Set<QuestionGeneric<?, ?>> targets = Question.get(Q1).targets();
        assertThat(targets, not(hasItems(Question.get(Q2))));
        assertThat(targets, hasItems(Question.get(Q3)));
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
    @DisplayName("Throws an exception on already existing label with different type")
    void creatingQuestionTwice_WithDifferentTypes_throwsException() {
        QuestionInt.of(Q1);
        assertThrows(
                IllegalStateException.class,
                () -> QuestionBool.of(Q1),
                "Found question [Q1] with different type [QuestionBool] than requested [QuestionInt]"
        );
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
    @DisplayName("Contains target")
    void isLinkedInFlow() {
        Question.of(Q1).targetGet(Question.of(Q2)).targetGet(Question.of(Q3)).targetGet(Question.of(Q4));
        assertThat(Question.of(Q1).containsTarget(Question.of(Q3)), is(true));
        assertThat(Question.of(Q1).containsTarget(Question.of(Q5)), is(false));
    }

    @Test
    @DisplayName("Get parent from flow member")
    void getParentFromFlow() {
        Question.of(Q1).targetGet(Question.of(Q2)).targetGet(Question.of(Q3)).targetGet(Question.of(Q4));
        assertThat(Question.of(Q1).getParentsOf(Question.of(Q3)), hasSize(1));
        assertThat(Question.of(Q1).getParentsOf(Question.of(Q3)).iterator().next(), is(Question.of(Q2)));
    }

    @Test
    @DisplayName("match SurveyAnswer")
    void match() {
        assertThat(Question.of(Q1).match(new HistoryItem("Q1", null, true, false)), is(true));
        assertThat(Question.of(Q1).match(new HistoryItem("Q2", null, true, false)), is(false));
    }

    @Test
    @DisplayName("on back should trigger function")
    void onBack() {
        final AtomicBoolean isBackTriggered = new AtomicBoolean(false);
        Question.of(Q1).onBack(answer -> isBackTriggered.set(true));
        assertThat(isBackTriggered.get(), is(false));

        Question.of(Q1).onBack("this triggers on back");
        assertThat(isBackTriggered.get(), is(true));
    }

    @Test
    @DisplayName("AnswerRoute [COV]")
    void checkAnswerRoute() {
        final CustomCondition customChoice = new CustomCondition();
        QuestionGeneric.AnswerRoute<String> route = new QuestionGeneric.AnswerRoute<>(Question.of(Q1), null, customChoice);
        assertThat(route.target(), is(Question.of(Q1)));
        assertThat(route.getLabel(), is(customChoice.getLabel()));
        assertThat(route.equals(new QuestionGeneric.AnswerRoute<>(Question.of(Q1), null, customChoice)), is(true));
        assertThat(route.toString(), is(containsString("AnswerRoute{target=Question{label='Q1'}")));
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