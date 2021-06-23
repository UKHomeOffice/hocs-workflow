package uk.gov.digital.ho.hocs.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveStage {
    @Getter
    private Stage[] stages;
}
