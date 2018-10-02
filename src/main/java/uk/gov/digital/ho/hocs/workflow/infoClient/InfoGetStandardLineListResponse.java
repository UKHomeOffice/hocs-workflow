package uk.gov.digital.ho.hocs.workflow.infoClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@Getter
public class InfoGetStandardLineListResponse {

    @JsonProperty("standardLine")
    private Set<InfoStandardLine> standardLines;
}
