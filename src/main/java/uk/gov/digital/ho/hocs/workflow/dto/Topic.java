package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class Topic {

    @JsonProperty("value")
    private final UUID value;

    @JsonProperty("label")
    private final String label;

}
