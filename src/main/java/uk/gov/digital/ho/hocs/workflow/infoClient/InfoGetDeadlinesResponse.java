package uk.gov.digital.ho.hocs.workflow.infoClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.model.Deadline;

import java.util.Set;

@NoArgsConstructor
@Getter
public class InfoGetDeadlinesResponse {

    @JsonProperty("deadlines")
    private Set<Deadline> deadlines;
}
