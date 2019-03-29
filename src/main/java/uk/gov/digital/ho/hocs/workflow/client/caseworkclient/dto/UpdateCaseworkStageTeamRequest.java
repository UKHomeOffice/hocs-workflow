package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class UpdateCaseworkStageTeamRequest {

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("allocationType")
    private String allocationType;

    public UpdateCaseworkStageTeamRequest(UUID teamUUID, String allocationType) {
        this.teamUUID = teamUUID;
        this.allocationType = allocationType;
    }
}
