package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class AllocateCaseRequest {

    @JsonProperty("userUUID")
    private UUID userUUID;

    @JsonProperty("teamUUID")
    private UUID teamUUID;
}
