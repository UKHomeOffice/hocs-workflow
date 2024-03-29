package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor()
@NoArgsConstructor
@Getter
public class StageDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("stageType")
    private String type;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

}
