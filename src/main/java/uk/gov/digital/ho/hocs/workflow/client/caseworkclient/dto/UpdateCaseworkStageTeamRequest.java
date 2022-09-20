package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class UpdateCaseworkStageTeamRequest {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("allocationType")
    private String allocationType;

    public UpdateCaseworkStageTeamRequest(UUID caseUUID, UUID stageUUID, UUID teamUUID, String allocationType) {
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.teamUUID = teamUUID;
        this.allocationType = allocationType;
    }

}
