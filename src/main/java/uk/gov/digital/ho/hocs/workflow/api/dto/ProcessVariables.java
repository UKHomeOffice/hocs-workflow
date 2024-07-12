package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Optional;

public record ProcessVariables(@JsonProperty("processKey") String processKey,
                               @JsonProperty("businessKey") String businessKey,
                               @JsonProperty("rootProcessKey") @Nullable String rootProcessKey,
                               @JsonProperty("variables") Map<String, Optional<String>> variables) {}
