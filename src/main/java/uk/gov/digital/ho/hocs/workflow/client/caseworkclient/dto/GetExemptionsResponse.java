package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class GetExemptionsResponse {

    @JsonProperty("exemptions")
    @Getter
    Set<GetExemptionResponse> exemptions;

}
