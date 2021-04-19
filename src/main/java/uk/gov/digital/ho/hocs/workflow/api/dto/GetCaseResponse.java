package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsCaseSchema;
import java.util.Map;

@AllArgsConstructor
@Getter
public class GetCaseResponse {

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("schema")
    private HocsCaseSchema schema;

    @Setter
    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("caseType")
    private String caseType;

}
