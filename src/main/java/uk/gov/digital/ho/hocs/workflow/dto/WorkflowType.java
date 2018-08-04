package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WorkflowType {

    @JsonProperty("label")
    private String displayName;

    @JsonProperty("value")
    private String caseType;
}
