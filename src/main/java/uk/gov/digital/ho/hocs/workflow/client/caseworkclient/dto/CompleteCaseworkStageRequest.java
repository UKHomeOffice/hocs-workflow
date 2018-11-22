package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class CompleteCaseworkStageRequest {

    @JsonProperty("command")
    private String command = "complete_stage_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    private UUID stageUUID;


    public CompleteCaseworkStageRequest(UUID caseUUID, UUID stageUUID) {
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
    }
}
