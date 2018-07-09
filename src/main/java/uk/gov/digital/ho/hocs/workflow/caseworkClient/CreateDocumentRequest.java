package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
class CreateDocumentRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private CaseworkDocumentType type;

}
