# Surveys (Beta)
Survey is a plain java library to provide a base for surveys / questionnaires.
It also provides a function to generate diagrams and to measure answer times.

# Motivation 
The goal of this project was to build a simple, solid core library with a minimalistic style.
Means everyone can build easily on top of it.
There are no framework, no reflections, no complicated nested objects and no big dependencies (except the diagram thingy). 
A survey is easy to store in a database and to modify as its just a simple ordered list. 

[![Build][build_shield]][build_link]
[![Maintainable][maintainable_shield]][maintainable_link]
[![Coverage][coverage_shield]][coverage_link]
[![Issues][issues_shield]][issues_link]
[![Commit][commit_shield]][commit_link]
[![Dependencies][dependency_shield]][dependency_link]
[![License][license_shield]][license_link]
[![Central][central_shield]][central_link]
[![Tag][tag_shield]][tag_link]
[![Javadoc][javadoc_shield]][javadoc_link]
[![Size][size_shield]][size_shield]
![Label][label_shield]

### Features
- [x] Create a flow (e.g. `Questionof("Q1").target(Questionof("Q2"));`)  
  - [x] Create a flow with condition (e.g. `Questionof("Q1").target(Questionof("Q2"), answer -> answer.equals("yes");`)  
  - [x] Create a flow with condition with a custom object (e.g. `Questionof("Q1").target(Questionof("Q2"), new CustomChoice();`)  
- [x] Create a custom question (e.g. `public class MyQuestion extends QuestionGeneric<Boolean, QuestionBool>`)  
- [x] Create Survey with history and states (e.g. `Survey.init(Question.of(Q1).target(Question.of(Q2)));`)
- [x] Answer current survey question (e.g. `Survey.init(Question.of(Q1)).answer("yes");`)
- [x] Export Survey (e.g. `Survey.init(Question.of(Q1)).getHistory();`)
- [x] Import Survey (e.g. `Survey.init(history);`)
- [x] Back/Forward transitions for a Survey state (e.g. `Survey.init(Questionof("Q1").target(Questionof("Q2")).answer("yes").transitTo(Questionof("Q2"));`)
  - [x] Setup on back trigger transition (e.g. `Question.of(Q1).onBack(oldAanswer -> isBackTriggered.set(true));`) (Will be triggered automatically on any back transition which steps over this question - it can also block the back transition)  
  - [ ] Setup on back trigger transition with a custom object (e.g. `Question.of(Q1).onBack(oldAanswer -> new CustomBack();`)  
  - [ ] Detect circular flow  
- [x] Render diagram from a Survey (e.g. `SurveyDiagram.render(Questionof("Q1").target(Questionof("Q2"), null, format);`)  
- [x] Get answer times (e.g. `Survey.init(Question.of(Q1)).answer("something").getDurationsMS();`)  

### Usage example
```java
class SurveyExampleTest {

    @Test
    void testSurvey() {
        final QuestionBool start = QuestionBool.of("START");
        final AtomicBoolean question2BackTriggered = new AtomicBoolean(false);

        //DEFINE FLOW
        start.target(Question.of("Q1_TRUE"), answer -> answer == true);
        start.targetGet(Question.of("Q1_FALSE"), answer -> answer == false)
                .targetGet(Question.of("Q2")).onBack(oldAnswer -> question2BackTriggered.set(true))
                .targetGet(Question.of("Q3"))
                .targetGet(Question.of("END"));

        //CREATE survey that manages the history / context
        final Survey survey01 = Survey.init(start);

        //EXECUTE survey flow
        assertThat(survey01.get(), is(equalTo(QuestionBool.of("START"))));
        assertThat(survey01.answer("Yes").get(), is(equalTo(Question.of("Q1_TRUE"))));
        assertThat(survey01.transitTo("START"), is(true));

        //TRANSITION NO BACK TRIGGERED
        assertThat(question2BackTriggered.get(), is(false));
        assertThat(survey01.get(), is(equalTo(QuestionBool.of("START"))));
        assertThat(survey01.answer("No").get(), is(equalTo(Question.of("Q1_FALSE"))));

        //EXPORT / IMPORT
        List<SurveyAnswer> export = survey01.getHistory();
        final Survey survey02 = Survey.init(export);
        assertThat(export, is(equalTo(survey02.getHistory())));
        assertThat(survey02.get(), is(equalTo(Question.of("Q1_FALSE"))));
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
        assertThat(Survey.init(survey02.getHistory()).isEnded(), is(true));
    }
}
```

### Diagram example
![Diagram example](src/test/resources/diagram_png_example.png)

[build_shield]: https://github.com/YunaBraska/surveys/workflows/JAVA_CI/badge.svg
[build_link]: https://github.com/YunaBraska/surveys/actions
[maintainable_shield]: https://img.shields.io/codeclimate/maintainability/YunaBraska/surveys?style=flat-square
[maintainable_link]: https://codeclimate.com/github/YunaBraska/surveys/maintainability
[coverage_shield]: https://img.shields.io/codecov/c/github/YunaBraska/surveys?style=flat-square
[coverage_link]: https://codecov.io/gh/YunaBraska/surveys?branch=master
[issues_shield]: https://img.shields.io/github/issues/YunaBraska/surveys?style=flat-square
[issues_link]: https://github.com/YunaBraska/surveys/commits/master
[commit_shield]: https://img.shields.io/github/last-commit/YunaBraska/surveys?style=flat-square
[commit_link]: https://github.com/YunaBraska/surveys/issues
[license_shield]: https://img.shields.io/github/license/YunaBraska/surveys?style=flat-square
[license_link]: https://github.com/YunaBraska/surveys/blob/master/LICENSE
[dependency_shield]: https://img.shields.io/librariesio/github/YunaBraska/surveys?style=flat-square
[dependency_link]: https://libraries.io/github/YunaBraska/surveys
[central_shield]: https://img.shields.io/maven-central/v/berlin.yuna/surveys?style=flat-square
[central_link]:https://search.maven.org/artifact/berlin.yuna/surveys
[tag_shield]: https://img.shields.io/github/v/tag/YunaBraska/surveys?style=flat-square
[tag_link]: https://github.com/YunaBraska/surveys/releases
[javadoc_shield]: https://javadoc.io/badge2/berlin.yuna/surveys/javadoc.svg?style=flat-square
[javadoc_link]: https://javadoc.io/doc/berlin.yuna/surveys
[size_shield]: https://img.shields.io/github/repo-size/YunaBraska/surveys?style=flat-square
[label_shield]: https://img.shields.io/badge/Yuna-QueenInside-blueviolet?style=flat-square
[gitter_shield]: https://img.shields.io/gitter/room/YunaBraska/surveys?style=flat-square
[gitter_link]: https://gitter.im/surveys/Lobby