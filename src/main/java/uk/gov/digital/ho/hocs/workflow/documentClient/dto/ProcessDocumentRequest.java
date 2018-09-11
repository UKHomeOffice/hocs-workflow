package uk.gov.digital.ho.hocs.workflow.documentClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public class ProcessDocumentRequest {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("fileLink")
    private String fileLink;
}
