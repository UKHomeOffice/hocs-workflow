package uk.gov.digital.ho.hocs.workflow.client.infoClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class InfoGetTemplateResponse {

    @JsonProperty("label")
    private String displayName;

    @JsonProperty("value")
    private UUID uuid;
}
