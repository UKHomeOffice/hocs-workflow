package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MigrationRequest {

    @JsonProperty
    private String source;

    @JsonProperty
    private String target;

    @JsonProperty
    private String action;
}
