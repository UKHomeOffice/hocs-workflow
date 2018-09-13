package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public class UpdateCaseworkInputDataRequest {

    @JsonProperty("command")
    private String command = "update_input_data_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("data")
    private Map<String, String> data;

    public UpdateCaseworkInputDataRequest(UUID caseUUID, Map<String,String> data) {
        this.caseUUID = caseUUID;
        this.data = data;
    }
}