package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.CaseType;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Getter
class CreateCaseRequest {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("caseType")
    private CaseType caseType;
}
