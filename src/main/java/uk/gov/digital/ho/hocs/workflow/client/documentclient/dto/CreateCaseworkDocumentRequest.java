package uk.gov.digital.ho.hocs.workflow.client.documentclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseworkDocumentRequest {

    @JsonProperty("name")
    private String name;

    public CreateCaseworkDocumentRequest(String name, String type, String fileLink, UUID externalReferenceUUID) {
        this.name = name;
        this.type = type;
        this.fileLink = fileLink;
        this.externalReferenceUUID = externalReferenceUUID;
    }

    @JsonProperty("type")
    private String type;

    @JsonProperty("fileLink")
    private String fileLink;

    @JsonProperty("externalReferenceUUID")
    private UUID externalReferenceUUID;

    @JsonProperty("actionDataItemUuid")
    private UUID actionDataItemUuid;
}
