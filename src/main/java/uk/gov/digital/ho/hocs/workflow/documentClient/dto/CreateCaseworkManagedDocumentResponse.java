package uk.gov.digital.ho.hocs.workflow.documentClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class CreateCaseworkManagedDocumentResponse {

    @JsonProperty("uuid")
    private UUID uuid;

}
