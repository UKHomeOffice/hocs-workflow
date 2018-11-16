package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor()
@NoArgsConstructor
@Getter
public class Topic {

    @JsonProperty("label")
    private String label;

    @JsonProperty("value")
    private UUID value;

}
