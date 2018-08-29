package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.ReferenceType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateCaseworkReferenceRequest {

    @JsonProperty("type")
    private ReferenceType type;

    @JsonProperty("reference")
    private String reference;
}
