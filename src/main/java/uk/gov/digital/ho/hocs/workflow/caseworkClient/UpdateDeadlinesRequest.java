package uk.gov.digital.ho.hocs.workflow.caseworkClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoDeadlines;

import java.util.Set;

@AllArgsConstructor
@Getter
public class UpdateDeadlinesRequest {

    @JsonProperty("deadlines")
    private Set<InfoDeadlines> deadlines;


}
