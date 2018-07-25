package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

@AllArgsConstructor
public class WorkflowType {

    @JsonProperty("requiredRole")
    private String requiredRole;

    @JsonProperty("label")
    private String displayName;

    @JsonProperty("value")
    private CaseType caseType;
}
