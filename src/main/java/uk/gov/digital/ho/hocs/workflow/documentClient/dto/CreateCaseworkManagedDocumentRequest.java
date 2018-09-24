package uk.gov.digital.ho.hocs.workflow.documentClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.documentClient.model.ManagedDocumentType;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseworkManagedDocumentRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private ManagedDocumentType type;

    @JsonProperty("externalReferenceUUID")
    private UUID externalReferenceUUID;

}