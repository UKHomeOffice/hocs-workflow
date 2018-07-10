package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Getter
class CreateCaseRequest {

    @JsonProperty("caseUUID")
    @NonNull
    private UUID caseUUID;

    @JsonProperty("caseType")
    @NonNull
    private CaseType caseType;

    @JsonProperty("documentSummaries")
    private List<CaseworkDocumentSummary> documents;
}
