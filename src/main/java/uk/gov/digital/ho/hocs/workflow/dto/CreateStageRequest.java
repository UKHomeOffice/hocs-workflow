package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

@AllArgsConstructor
public class CreateStageRequest {

    @JsonProperty("stageType")
    private StageType stageType;
}
