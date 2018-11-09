package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.digital.ho.hocs.workflow.model.CaseNoteType;

import java.util.UUID;

public class CreateCaseworkCaseNoteRequest {

    @JsonProperty("command")
    private String command = "create_case_note_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("caseNoteType")
    private CaseNoteType caseNoteType;

    @JsonProperty("casenote")
    private String caseNote;

    public CreateCaseworkCaseNoteRequest(UUID caseUUID, CaseNoteType caseNoteType, String caseNote) {
        this.caseUUID = caseUUID;
        this.caseNoteType = caseNoteType;
        this.caseNote = caseNote;
    }
}