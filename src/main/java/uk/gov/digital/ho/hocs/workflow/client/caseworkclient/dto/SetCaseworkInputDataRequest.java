package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class SetCaseworkInputDataRequest {

    @JsonProperty("data")
    private Map<String, String> data;
}
