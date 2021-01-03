package berlin.yuna.survey.logic;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.LocalDateTime.now;

public class CommonUtils {

    private CommonUtils() {
    }

    public static boolean hasText(final Object string) {
        return toText(string).isPresent();
    }

    public static Optional<String> toText(final Object string) {
        return string instanceof String && ((String) string).trim().length() > 0 ? Optional.of((String) string) : Optional.empty();
    }

    public static LocalDateTime getTime() {
        return now(ZoneId.of("UTC"));
    }
}
