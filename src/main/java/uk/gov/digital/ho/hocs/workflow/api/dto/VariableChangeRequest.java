package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VariableChangeRequest {

    @JsonProperty("variable")
    private String variable;

    @JsonProperty("value")
    private String value;
}
