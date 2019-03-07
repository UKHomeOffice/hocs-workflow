package uk.gov.digital.ho.hocs.workflow.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MigrationProgressCaseRequest {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("type")
    private CaseDataType type;

    @JsonProperty("data")
    private Map<String, String> data;

    @JsonProperty("seedData")
    private Map<String, String> seedData;

    @JsonProperty("documents")
    private List<DocumentSummary> documents;

    @JsonProperty("correspondent")
    private List<MigrationCorrespondent> correspondent;

    @JsonProperty("draftDocumentUUID")
    private UUID draftDocumentUUID;

    @JsonProperty("topic")
    private UUID topic;
}
