package uk.gov.digital.ho.hocs.workflow.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = "displayCode")
public class CaseType {

    @JsonProperty("displayCode")
    private String displayCode;

    @JsonProperty("shortCode")
    private String shortCode;
}