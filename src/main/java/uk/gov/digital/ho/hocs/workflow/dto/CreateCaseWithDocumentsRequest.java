package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateCaseWithDocumentsRequest {

    @JsonProperty("type")
    private CaseType caseType;

    private List<DocumentSummary> documentSummaries;
}
