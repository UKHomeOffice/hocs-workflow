package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class GetCaseworkStageResponse {


    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("caseType")
    private CaseDataType caseType;

    @JsonProperty("data")
    private Map<String,String> data;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("stageType")
    private StageType stageType;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;

}
