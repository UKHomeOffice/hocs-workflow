package uk.gov.digital.ho.hocs.workflow.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class MigrationCreateCaseworkCaseRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("ref")
    private String caseReference;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("received")
    private LocalDate dateReceived;

    @JsonProperty("deadline")
    private LocalDate caseDeadline;

    @JsonProperty("notes")
    private List<String> notes;

    @JsonProperty("totalsListName")
    private String totalsListName;
}
