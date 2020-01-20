package uk.gov.digital.ho.hocs.workflow.client.infoclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor()
@NoArgsConstructor()
@Getter
public class CaseDetailsFieldDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("component")
    private String component;

    @JsonProperty("props")
    private Map<String, Object> props;

}
