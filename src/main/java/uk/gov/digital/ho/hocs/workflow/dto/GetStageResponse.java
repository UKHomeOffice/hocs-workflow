package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetStageResponse {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("screenName")
    private String screenName;

    @JsonProperty("form")
    private HocsForm form;
}
