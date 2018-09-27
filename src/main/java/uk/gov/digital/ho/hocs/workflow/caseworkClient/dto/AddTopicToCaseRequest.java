package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class AddTopicToCaseRequest {

    @JsonProperty("topicUUID")
    private UUID topicUUID;

    @JsonProperty("topicName")
    private String topicName;
}
