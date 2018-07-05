package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class CreateStageRequest {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("stageType")
    private StageType stageType;

    @JsonProperty("stageData")
    private Map<String, String> stageData;
}
