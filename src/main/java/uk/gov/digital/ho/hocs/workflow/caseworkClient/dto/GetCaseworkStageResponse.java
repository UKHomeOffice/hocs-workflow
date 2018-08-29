package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class GetCaseworkStageResponse {

    @JsonProperty("type")
    private StageType type;

    @JsonProperty("data")
    private String data;

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("caseReference")
    private String caseReference;

}
