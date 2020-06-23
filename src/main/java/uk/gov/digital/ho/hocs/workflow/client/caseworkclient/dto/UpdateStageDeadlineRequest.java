package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class UpdateStageDeadlineRequest {

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("days")
    private Integer days;

    public UpdateStageDeadlineRequest(
            String stageType,
            Integer days) {
        this.stageType = stageType;
        this.days = days;
    }
}
