package berlin.yuna.survey.logic;

import berlin.yuna.survey.model.types.QuestionGeneric;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.format;

public class QuestionHelper {

    public static Optional<QuestionGeneric<?, ?>> searchInFlow(final QuestionGeneric<?,?> search, final QuestionGeneric<?, ?> flowItem) {
        Optional<QuestionGeneric<?, ?>> result = searchInFlow(search.label(), flowItem);
        if(result.isPresent() && search.getClass() != result.get().getClass()){
            throw new IllegalStateException(format(
                    "Question [%s] is already defined with different type [%s] than requested [%s]",
                    search.label(),
                    result.get().getClass().getSimpleName(),
                    search.getClass().getSimpleName()
            ));
        }
        return result;
    }

    public static Optional<QuestionGeneric<?, ?>> searchInFlow(final String search, final QuestionGeneric<?, ?> flowItem) {
        return searchInFlow(flowItem, search, new HashSet<>());
    }

    private static Optional<QuestionGeneric<?, ?>> searchInFlow(final QuestionGeneric<?, ?> current, final String search, final HashSet<String> checked) {
        if (current.label().equals(search)) {
            return Optional.of(current);
        } else if (!checked.contains(current.label())) {
            checked.add(current.label());
            return Stream.concat(current.targets().stream(), current.parents().stream()).flatMap(q -> searchInFlow(q, search, checked).stream()).findFirst();
        } else {
            return Optional.empty();
        }
    }
}
