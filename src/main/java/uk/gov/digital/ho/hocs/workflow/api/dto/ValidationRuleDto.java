package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor()
@NoArgsConstructor()
@Getter
public class ValidationRuleDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("title")
    private String title;

    @JsonRawValue
    private Map<String, Object> subSchema;

}
