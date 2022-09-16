package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDocumentRequest {

    @JsonProperty("documents")
    private List<DocumentSummary> documents;

    @JsonProperty("actionDataItemUuid")
    private UUID actionDataItemUuid;

}
