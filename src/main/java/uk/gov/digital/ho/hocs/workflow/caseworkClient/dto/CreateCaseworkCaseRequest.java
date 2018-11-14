package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseDataType;

@AllArgsConstructor
public class CreateCaseworkCaseRequest {

    @JsonProperty("type")
    private CaseDataType type;
}
