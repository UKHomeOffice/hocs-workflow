package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.dto.GetCaseworkDocumentsResponse;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetDocumentsResponse {

    @JsonProperty("documents")
    private Set<GetDocumentResponse> documents;

    public static GetDocumentsResponse from(GetCaseworkDocumentsResponse documents) {
        Set<GetDocumentResponse> documentResponses = documents.getDocuments()
                .stream()
                .map(GetDocumentResponse::from)
                .collect(Collectors.toSet());

        return new GetDocumentsResponse(documentResponses);
    }
}