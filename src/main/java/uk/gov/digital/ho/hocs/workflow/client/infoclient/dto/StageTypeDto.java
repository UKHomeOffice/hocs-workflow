package uk.gov.digital.ho.hocs.workflow.client.infoclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Getter
public class StageTypeDto implements Serializable {

    //TODO: remove - used in UI
    @JsonProperty("label")
    private String label;

    //TODO: remove - used in UI
    @JsonProperty("value")
    private String value;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("shortCode")
    private String shortCode;

    @JsonProperty("type")
    private String type;

    public StageTypeDto(String label, String value, String displayName, String type, String shortCode) {
        this.label = label;
        this.value = value;
        this.displayName = displayName;
        this.shortCode = shortCode;
        this.type = type;
    }

}
