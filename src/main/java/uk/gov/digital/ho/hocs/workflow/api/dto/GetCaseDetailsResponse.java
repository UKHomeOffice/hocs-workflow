package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsSchema;

import java.util.Map;

@AllArgsConstructor
@Getter
public class GetCaseDetailsResponse {

    @JsonProperty("schema")
    private HocsSchema schema;

    @JsonProperty("data")
    private Map<String, String> data;
}
