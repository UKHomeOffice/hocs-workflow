package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.digital.ho.hocs.workflow.model.CaseDataType;

import java.util.Map;

@AllArgsConstructor
public class CreateCaseworkCaseRequest {

    @JsonProperty("type")
    private CaseDataType type;

    @JsonProperty("data")
    private Map<String, String> data;
}
