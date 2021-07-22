package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;

import java.util.UUID;

@AllArgsConstructor
@Getter
@ToString
public class GetStageResponse {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("form")
    private HocsForm form;
}
