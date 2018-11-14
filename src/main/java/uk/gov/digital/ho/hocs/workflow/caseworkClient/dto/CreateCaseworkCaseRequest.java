package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

@AllArgsConstructor
public class CreateCaseworkCaseRequest {

    @JsonProperty("type")
    private CaseType type;
}
