package uk.gov.digital.ho.hocs.workflow.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseType;

import java.time.LocalDate;
import java.util.Map;

@AllArgsConstructor
@Getter
public class MigrationCreateCaseworkCaseRequest {

    @JsonProperty("type")
    private CaseType type;

    @JsonProperty("ref")
    private String caseReference;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("received")
    private LocalDate dateReceived;

    @JsonProperty("deadline")
    private LocalDate caseDeadline;
}
