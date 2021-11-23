package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public class UpdateCaseworkCaseDataRequest {

    @JsonProperty("data")
    private Map<String, String> data;

    public UpdateCaseworkCaseDataRequest(Map<String,String> data) {
        this.data = data;
    }
}