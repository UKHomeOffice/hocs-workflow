package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateCaseWithDocumentsRequest {

    @JsonProperty("type")
    private CaseDataType caseDataType;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("documents")
    private List<DocumentSummary> documents;
}
