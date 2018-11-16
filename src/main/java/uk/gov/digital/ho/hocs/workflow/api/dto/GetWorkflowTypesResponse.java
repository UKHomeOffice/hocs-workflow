package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GetWorkflowTypesResponse {

    @JsonProperty("workflowTypes")
    private List<WorkflowType> workflowTypes;
}
