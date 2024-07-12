package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record GetProcessVariablesResponse(
    @JsonProperty("caseUUID") UUID caseUUID,
    @JsonProperty("stageUUID") UUID stageUUID,
    @JsonProperty("processes") List<ProcessVariables> processes) {}
