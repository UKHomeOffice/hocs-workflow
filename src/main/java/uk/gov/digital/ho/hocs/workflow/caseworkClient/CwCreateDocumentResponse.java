package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CwCreateDocumentResponse {

    @JsonProperty("uuid")
    private final UUID uuid;
}
