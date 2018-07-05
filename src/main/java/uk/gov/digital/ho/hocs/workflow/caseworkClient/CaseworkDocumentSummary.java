package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.model.DocumentType;
import uk.gov.digital.ho.hocs.workflow.dto.DocumentSummary;

import java.util.UUID;

@Getter
public class CaseworkDocumentSummary {

    @JsonProperty("UUID")
    private UUID documentUUID;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("type")
    private DocumentType type;


    // We don't need to send e.g. S3 untrusted url to the casework service
    private CaseworkDocumentSummary(UUID documentUUID, String displayName, DocumentType type)
    {

        this.documentUUID = documentUUID;
        this.displayName = displayName;
        this.type = type;
    }

    public static CaseworkDocumentSummary from(DocumentSummary documentSummary) {
        return new CaseworkDocumentSummary(documentSummary.getDocumentUUID(), documentSummary.getDisplayName(), documentSummary.getType());
    }
}
