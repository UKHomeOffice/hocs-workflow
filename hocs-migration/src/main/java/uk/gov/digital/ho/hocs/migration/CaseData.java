package uk.gov.digital.ho.hocs.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class CaseData {

    @JsonProperty("CaseworkTeamUUID")
    final private String caseworkTeamUuid;
}
