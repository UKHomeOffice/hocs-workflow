package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Getter
public class UpdateCaseworkTeamStageTextResponse {

    @JsonProperty("teamMap")
    private Map<String, String> teamMap;
}
