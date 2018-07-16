package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.model.StageType;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class CreateCaseResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    @JsonProperty("reference")
    private final String reference;
}
