package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;

@AllArgsConstructor
@Getter
public class GetFormResponse {

    @JsonProperty("caseReference")
    private final String caseReference;

    @JsonProperty("form")
    private final HocsForm form;

}
