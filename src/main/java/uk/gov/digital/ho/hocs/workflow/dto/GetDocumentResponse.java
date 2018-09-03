package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.GetCaseworkDocumentResponse;
import uk.gov.digital.ho.hocs.workflow.model.DocumentStatus;
import uk.gov.digital.ho.hocs.workflow.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetDocumentResponse {

    @JsonProperty("type")
    private DocumentType type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("s3_orig_link")
    private String fileLink;

    @JsonProperty("s3_pdf_link")
    private String pdfLink;

    @JsonProperty("status")
    private DocumentStatus status;

    @JsonProperty("document_uuid")
    private UUID uuid;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("deleted")
    private Boolean deleted;

    public static GetDocumentResponse from(GetCaseworkDocumentResponse response) {
        return new GetDocumentResponse(
                response.getType(),
                response.getName(),
                response.getFileLink(),
                response.getPdfLink(),
                response.getStatus(),
                response.getUuid(),
                response.getTimestamp(),
                response.getDeleted()
        );
    }
}