package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.UUID;

public class UpdateCaseworkStageUserRequest {

    @JsonProperty("command")
    private String command = "update_stage_user_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;

    public UpdateCaseworkStageUserRequest(UUID caseUUID, UUID stageUUID, UUID userUUID) {
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.userUUID = userUUID;
    }
}
