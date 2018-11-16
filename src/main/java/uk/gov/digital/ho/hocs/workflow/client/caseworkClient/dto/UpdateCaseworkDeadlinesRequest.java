package uk.gov.digital.ho.hocs.workflow.client.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.digital.ho.hocs.workflow.domain.model.StageType;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class UpdateCaseworkDeadlinesRequest {

    @JsonProperty("command")
    private String command = "update_deadlines_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("deadlines")
    private Map<StageType, LocalDate> deadlines;

    public UpdateCaseworkDeadlinesRequest(UUID caseUUID, Map<StageType, LocalDate> deadlines) {
        this.caseUUID = caseUUID;
        this.deadlines = deadlines;
    }

}