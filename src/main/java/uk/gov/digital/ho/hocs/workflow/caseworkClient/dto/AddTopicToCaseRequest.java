package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.UUID;

public class AddTopicToCaseRequest {

    @JsonProperty("command")
    private String command = "create_topic_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("topicNameUUID")
    private UUID topicUUID;

    @JsonProperty("topicName")
    private String topicName;

    public AddTopicToCaseRequest(UUID caseUUID, UUID topicUUID, String topicName) {
        this.caseUUID = caseUUID;
        this.topicUUID = topicUUID;
        this.topicName = topicName;
    }
}