package uk.gov.digital.ho.hocs.workflow.client.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class CreateCaseworkCaseResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("reference")
    private String reference;

}
