package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class AddDocumentsRequest {

    @JsonProperty("documentSummaries")
    private List<DocumentSummary> documentSummaries;
}
