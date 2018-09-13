package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.digital.ho.hocs.workflow.model.ReferenceType;

import java.util.UUID;

public class CreateCaseworkReferenceRequest {

    @JsonProperty("command")
    private String command = "create_reference_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("type")
    private ReferenceType type;

    @JsonProperty("reference")
    private String reference;

    public CreateCaseworkReferenceRequest(UUID caseUUID, String reference, ReferenceType type) {
        this.caseUUID = caseUUID;
        this.reference = reference;
        this.type = type;
    }
}