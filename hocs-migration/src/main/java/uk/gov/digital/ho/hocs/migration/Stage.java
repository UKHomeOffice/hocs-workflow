package uk.gov.digital.ho.hocs.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class Stage {
    private String uuid;

    @JsonProperty("teamUUID")
    final private String teamUuid;

    @JsonProperty("caseUUID")
    final private String caseUuid;
}
