package uk.gov.digital.ho.hocs.workflow.domain.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.Schema;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ScreenRepository {

    private final ObjectMapper objectMapper;

    protected ScreenRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Schema getSchema(String screenName) {
        return readScreenFromFile(screenName.trim());
    }

    private Schema readScreenFromFile(String screenName) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(
            String.format("screens/%s.json", screenName))) {

            if (in == null) {
                throw new ApplicationExceptions.ScreenNotFoundException(
                    String.format("Screen %s could not be found", screenName), LogEvent.SCREEN_NOT_FOUND);
            }
            return objectMapper.readValue(in, new TypeReference<>() {});
        } catch (IOException e) {
            throw new ApplicationExceptions.JsonFileReadException(
                String.format("Unable to read file: %s into type %s", screenName, Schema.class.getSimpleName()),
                LogEvent.CONFIG_PARSE_FAILURE);
        }
    }

}
