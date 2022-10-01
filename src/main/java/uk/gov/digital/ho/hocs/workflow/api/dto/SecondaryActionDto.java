package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Deprecated(forRemoval = true)
public class SecondaryActionDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("name")
    private String name;

    @JsonProperty("label")
    private String label;

    @JsonProperty("component")
    private String component;

    @JsonRawValue
    private String[] validation;

    @JsonProperty("props")
    private Map<String, Object> props;

}
