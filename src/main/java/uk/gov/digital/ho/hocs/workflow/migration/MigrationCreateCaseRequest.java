package uk.gov.digital.ho.hocs.workflow.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MigrationCreateCaseRequest {

    @JsonProperty("type")
    private CaseDataType type;

    @JsonProperty("ref")
    private String caseReference;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("caseDeadline")
    private LocalDate caseDeadline;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("documents")
    private List<DocumentSummary> documents;

    @JsonProperty("correspondent")
    private List<MigrationCorrespondent> correspondent;

    @JsonProperty("topic")
    private UUID topic;
}
