package uk.gov.digital.ho.hocs.workflow.domain.repositories.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SecondaryAction {

    @JsonProperty("name")
    private String name;

    @JsonProperty("label")
    private String label;

    @JsonProperty("component")
    private String component;

    @JsonProperty("validation")
    private String[] validation;

    @JsonProperty("props")
    private Map<String, Object> props;

}
