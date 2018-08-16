package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class CwGetStageResponse {

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private String data;

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("caseReference")
    private String caseReference;

}
