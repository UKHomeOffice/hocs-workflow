package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseworkCaseRequest {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("type")
    private CaseDataType type;

    @JsonProperty("data")
    private Map<String, String> data;
}
