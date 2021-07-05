package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class CreateCaseRequest implements CreateCaseRequestInterface {

    @JsonProperty("type")
    private String type;

    @JsonProperty("dateReceived")
    private LocalDate dateReceived;

    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> data;

    @JsonProperty("documents")
    private List<DocumentSummary> documents;
}
