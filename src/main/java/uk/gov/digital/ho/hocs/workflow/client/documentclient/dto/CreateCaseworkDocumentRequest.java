package uk.gov.digital.ho.hocs.workflow.client.documentclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CreateCaseworkDocumentRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("fileLink")
    private String fileLink;

    @JsonProperty("externalReferenceUUID")
    private UUID externalReferenceUUID;
}
