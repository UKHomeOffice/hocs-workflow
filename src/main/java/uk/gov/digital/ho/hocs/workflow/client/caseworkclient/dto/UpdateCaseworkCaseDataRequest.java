package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public class UpdateCaseworkCaseDataRequest {

    @JsonProperty("command")
    private String command = "update_case_data_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("data")
    private Map<String, String> data;

    public UpdateCaseworkCaseDataRequest(UUID caseUUID, Map<String,String> data) {
        this.caseUUID = caseUUID;
        this.data = data;
    }
}