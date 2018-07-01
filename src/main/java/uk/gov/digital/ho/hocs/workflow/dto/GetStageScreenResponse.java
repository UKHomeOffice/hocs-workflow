package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetStageScreenResponse {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("screen")
    private String screen;
}
