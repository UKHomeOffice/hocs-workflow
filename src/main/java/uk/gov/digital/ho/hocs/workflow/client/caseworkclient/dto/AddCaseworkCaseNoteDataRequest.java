package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseNoteType;

import java.util.UUID;

public class AddCaseworkCaseNoteDataRequest {

    @JsonProperty("command")
    private String command = "create_case_note_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("caseNote")
    private String caseNote;

    @JsonProperty("caseNoteType")
    private CaseNoteType caseNoteType;

    public AddCaseworkCaseNoteDataRequest(UUID caseUUID, String caseNote, CaseNoteType caseNoteType) {
        this.caseUUID = caseUUID;
        this.caseNote = caseNote;
        this.caseNoteType = caseNoteType;
    }
}