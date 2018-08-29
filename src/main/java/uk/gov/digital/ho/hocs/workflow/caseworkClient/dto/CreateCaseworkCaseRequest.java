package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.time.LocalDate;

@AllArgsConstructor
public class CreateCaseworkCaseRequest {

    @JsonProperty("type")
    private CaseType type;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;
}
