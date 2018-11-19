package uk.gov.digital.ho.hocs.workflow.client.documentclient.dto;

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
