package uk.gov.digital.ho.hocs.workflow.client.infoClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class InfoTemplate {

    @JsonProperty("label")
    private String displayName;

    @JsonProperty("value")
    private UUID uuid;
}
