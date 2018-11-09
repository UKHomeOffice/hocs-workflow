package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.StageStatusType;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class CreateCaseworkStageResponse {

    @JsonProperty("uuid")
    private UUID uuid;
}