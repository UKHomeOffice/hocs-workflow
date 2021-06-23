package uk.gov.digital.ho.hocs.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StageData {

    @JsonProperty("CaseworkTeamUUID")
    private String caseworkTeamUuid;
}
