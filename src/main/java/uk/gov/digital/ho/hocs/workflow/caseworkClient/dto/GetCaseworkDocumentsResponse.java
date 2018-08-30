package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public class GetCaseworkDocumentsResponse {

    @JsonProperty("documents")
    private Set<GetCaseworkDocumentResponse> documents;

}