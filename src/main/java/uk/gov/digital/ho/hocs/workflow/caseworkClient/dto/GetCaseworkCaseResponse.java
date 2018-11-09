package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseDataType;

import java.util.Map;

@NoArgsConstructor
@Getter
public class GetCaseworkCaseResponse {

    @JsonProperty("type")
    private CaseDataType type;

    @JsonProperty("data")
    private Map<String,String> data;

}
