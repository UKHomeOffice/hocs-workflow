package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.DocumentStatus;
import uk.gov.digital.ho.hocs.workflow.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class GetCaseworkDocumentResponse {

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

}