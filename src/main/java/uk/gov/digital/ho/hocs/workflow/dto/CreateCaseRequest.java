package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateCaseRequest {

    @JsonProperty("type")
    private CaseType type;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("documents")
    private List<DocumentSummary> documents;
}
