package uk.gov.digital.ho.hocs.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Case {
    final private String uuid;

    @JsonProperty("data")
    final private CaseData caseData;
}
