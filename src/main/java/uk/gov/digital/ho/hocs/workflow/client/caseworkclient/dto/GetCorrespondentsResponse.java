package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@Getter
@NoArgsConstructor
public class GetCorrespondentsResponse {

    //@JsonProperty("correspondents")
    @Getter
    private List<GetCorrespondentResponse> correspondents;

}
