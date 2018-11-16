package uk.gov.digital.ho.hocs.workflow.client.documentClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class ProcessDocumentRequest {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("fileLink")
    private String fileLink;
}
