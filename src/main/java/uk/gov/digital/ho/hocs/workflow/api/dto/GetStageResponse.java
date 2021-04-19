package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetStageResponse {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("form")
    private HocsForm form;

    @JsonProperty
    private String caseType;
}
