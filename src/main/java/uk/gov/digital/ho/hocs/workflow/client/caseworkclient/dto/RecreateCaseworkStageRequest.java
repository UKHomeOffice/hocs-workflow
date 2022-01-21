package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
public class RecreateCaseworkStageRequest {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;

}
