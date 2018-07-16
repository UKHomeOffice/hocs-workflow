package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
class CreateStageRequest {

    @JsonProperty("type")
    private StageType stageType;

    @JsonProperty("data")
    private Map<String, String> stageData;
}
