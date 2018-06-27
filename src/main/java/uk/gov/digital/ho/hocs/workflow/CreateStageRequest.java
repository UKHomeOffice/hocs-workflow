package uk.gov.digital.ho.hocs.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class CreateStageRequest {

    @JsonProperty("stageType")
    private StageType stageType;
}
