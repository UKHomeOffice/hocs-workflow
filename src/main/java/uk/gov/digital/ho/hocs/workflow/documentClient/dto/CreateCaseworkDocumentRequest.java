package uk.gov.digital.ho.hocs.workflow.documentClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.documentClient.model.DocumentType;

import java.util.UUID;

@AllArgsConstructor
public class CreateCaseworkDocumentRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private DocumentType type;

    @JsonProperty("externalReferenceUUID")
    private UUID externalReferenceUUID;
}
