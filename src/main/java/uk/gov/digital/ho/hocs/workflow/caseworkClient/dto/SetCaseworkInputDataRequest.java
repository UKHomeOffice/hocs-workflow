package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class SetCaseworkInputDataRequest {

    @JsonProperty("data")
    private Map<String, String> data;
}
