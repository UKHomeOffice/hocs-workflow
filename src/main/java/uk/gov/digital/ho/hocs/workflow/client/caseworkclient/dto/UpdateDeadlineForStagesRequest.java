package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Map;

@Getter
public class UpdateDeadlineForStagesRequest {

    @JsonProperty("stageTypeAndDaysMap")
    private Map<String, Integer> stageTypeAndDaysMap;

    public UpdateDeadlineForStagesRequest(Map<String, Integer> stageTypeAndDaysMap) {
        this.stageTypeAndDaysMap = stageTypeAndDaysMap;
    }
}
