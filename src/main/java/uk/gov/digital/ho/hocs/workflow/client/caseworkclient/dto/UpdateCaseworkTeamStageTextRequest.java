package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UpdateCaseworkTeamStageTextRequest {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("teamUUIDKey")
    private String teamUUIDKey;

    @JsonProperty("teamNameKey")
    private String teamNameKey;

    @JsonProperty("texts")
    private String[] texts;

    public UpdateCaseworkTeamStageTextRequest(UUID caseUUID,
                                              UUID stageUUID,
                                              String stageType,
                                              String teamUUIDKey,
                                              String teamNameKey,
                                              String[] texts) {
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.stageType = stageType;
        this.teamUUIDKey = teamUUIDKey;
        this.teamNameKey = teamNameKey;
        this.texts = texts;
    }

}
