package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateTopicRequest {

    @NonNull
    @JsonProperty(value = "topicUUID", required = true)
    private final UUID topicUUID;

    public static CreateTopicRequest fromUUID(UUID uuidString) {
        return new CreateTopicRequest(uuidString);
    }

}
