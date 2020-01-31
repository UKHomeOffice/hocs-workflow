package uk.gov.digital.ho.hocs.workflow.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseRequestInterface;
import uk.gov.digital.ho.hocs.workflow.api.dto.DocumentSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MigrationCreateCaseRequest implements CreateCaseRequestInterface {

    @JsonProperty("type")
    private String type;

    @JsonProperty("ref")
    private String caseReference;

    @JsonProperty("startMessage")
    private String startMessage;

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
