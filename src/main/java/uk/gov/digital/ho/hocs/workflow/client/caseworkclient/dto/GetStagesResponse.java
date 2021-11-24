package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetStageResponse;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GetStagesResponse {

    @JsonProperty("stages")
    private Set<GetStageResponseDto> stages;

//    public static GetStagesResponse from(Set<StageDto> stages) {
//        Set<GetStageResponse> stageDataResponses = stages
//                .stream()
//                .map(GetStageResponse::from)
//                .collect(Collectors.toSet());
//
//        return new GetStagesResponse(stageDataResponses);
//    }
}