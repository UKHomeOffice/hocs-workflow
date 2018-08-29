package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.DocumentType;

@AllArgsConstructor
public class CreateCaseworkDocumentRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private DocumentType type;

}
