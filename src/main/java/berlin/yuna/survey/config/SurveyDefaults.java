package berlin.yuna.survey.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SurveyDefaults {

    private static final ObjectMapper mapper = init();

    public static ObjectMapper surveyMapper() {
        return mapper;
    }

    private static ObjectMapper init() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        return mapper;
    }

    private SurveyDefaults() {
    }
}
