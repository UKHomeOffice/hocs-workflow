package uk.gov.digital.ho.hocs.workflow.documentClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public class GetManagedDocumentsResponse {

    @JsonProperty("documents")
    private Set<ManagedDocumentDto> documentDtos;
}