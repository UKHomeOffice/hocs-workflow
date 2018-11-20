package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseNoteType;

import java.util.UUID;

@AllArgsConstructor
public class AddTopicToCaseRequest {

    @JsonProperty("command")
    private String command = "create_topic_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("topicNameUUID")
    private UUID topicNameUUID;

    @JsonProperty("topicName")
    private String topicName;


    public AddTopicToCaseRequest(UUID caseUUID, UUID topicNameUUID, String topicName) {
        this.caseUUID = caseUUID;
        this.topicNameUUID = topicNameUUID;
        this.topicName = topicName;
    }

}
