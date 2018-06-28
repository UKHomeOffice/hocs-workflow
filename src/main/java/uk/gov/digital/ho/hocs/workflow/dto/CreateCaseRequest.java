package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateCaseRequest {

    @JsonProperty("caseType")
    private CaseType caseType;
}
