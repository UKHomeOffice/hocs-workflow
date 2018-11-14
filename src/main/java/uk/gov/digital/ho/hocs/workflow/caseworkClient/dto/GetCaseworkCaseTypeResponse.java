package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseDataType;

@NoArgsConstructor
@Getter
public class GetCaseworkCaseTypeResponse {

    @JsonProperty("type")
    private CaseDataType type;


}
