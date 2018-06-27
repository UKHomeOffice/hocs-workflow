package uk.gov.digital.ho.hocs.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdateCaseRequest {

    @JsonProperty("caseType")
    private String caseType;

}
