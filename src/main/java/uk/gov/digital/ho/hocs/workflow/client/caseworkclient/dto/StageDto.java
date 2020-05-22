package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor()
@NoArgsConstructor
@Getter
public class StageDto {

    @JsonProperty("stageType")
    private String type;
}
