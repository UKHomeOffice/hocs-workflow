package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.domain.model.forms.HocsForm;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GetStageResponseDto {

    @JsonProperty("uuid")
    private UUID stageUUID;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("form")
    private HocsForm form;

}
