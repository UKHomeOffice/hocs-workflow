package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class CwCreateCaseResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("reference")
    private String reference;

}
