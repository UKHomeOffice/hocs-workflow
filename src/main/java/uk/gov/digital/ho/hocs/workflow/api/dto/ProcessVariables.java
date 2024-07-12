package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Optional;

public record ProcessVariables(@JsonProperty("processInstanceId") String processInstanceId,
                               @JsonProperty("businessKey") String businessKey,
                               @JsonProperty("rootProcessInstanceId") @Nullable String rootProcessInstanceId,
                               @JsonProperty("variables") Map<String, Optional<String>> variables) {}
