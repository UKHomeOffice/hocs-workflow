package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JumpToStepRequest {

    @JsonProperty("source")
    private String sourceStep;

    @JsonProperty("dest")
    private String destinationStep;
}
