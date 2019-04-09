package uk.gov.digital.ho.hocs.workflow.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"shortCode"})
public class CaseDataType {

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("shortCode")
    private String shortCode;

    @JsonProperty("type")
    private String displayCode;

    public CaseDataType(String type, String shortCode) {
        this.displayName = type;
        this.displayCode = type;
        this.shortCode = shortCode;
    }
}