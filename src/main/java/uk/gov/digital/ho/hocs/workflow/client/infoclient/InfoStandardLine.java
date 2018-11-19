package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class InfoStandardLine {

    @JsonProperty("label")
    private String displayName;

    @JsonProperty("value")
    private UUID uuid;
}
