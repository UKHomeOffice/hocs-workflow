package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class UpdateCaseworkStageTeamRequest {

    @JsonProperty("command")
    private String command = "update_stage_team_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    public UpdateCaseworkStageTeamRequest(UUID caseUUID, UUID stageUUID, UUID teamUUID) {
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.teamUUID = teamUUID;
    }
}
