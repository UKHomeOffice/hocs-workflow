package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class AddCaseworkCaseNoteDataRequest {

    @JsonProperty("command")
    private String command = "add_case_note_data_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("casenote")
    private String caseNote;

    public AddCaseworkCaseNoteDataRequest(UUID caseUUID, String caseNote) {
        this.caseUUID = caseUUID;
        this.caseNote = caseNote;
    }
}