package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MigrationRequest {

    @JsonProperty("source")
    private String source;

    @JsonProperty("target")
    private String target;
}
