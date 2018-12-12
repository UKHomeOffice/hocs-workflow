package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.domain.model.StageType;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseworkStageRequest {

    @JsonProperty("type")
    private StageType type;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("allocationType")
    private String allocationType;
}
