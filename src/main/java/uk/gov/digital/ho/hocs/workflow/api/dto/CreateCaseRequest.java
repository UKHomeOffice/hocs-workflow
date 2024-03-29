package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateCaseRequest implements CreateCaseRequestInterface {

    @JsonProperty("type")
    private String type;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("documents")
    private List<DocumentSummary> documents;

    @JsonProperty("fromCaseUUID")
    private UUID fromCaseUUID;

}
