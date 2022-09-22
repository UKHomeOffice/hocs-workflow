package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor()
@NoArgsConstructor
@Getter
public class GetAllStagesForCaseResponse {

    @JsonProperty("stages")
    private List<StageDto> stages;

}
