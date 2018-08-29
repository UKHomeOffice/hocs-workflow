package uk.gov.digital.ho.hocs.workflow.caseworkClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.model.Deadline;

import java.util.Set;


@AllArgsConstructor
@Getter
public class CreateCaseworkDeadlinesRequest {

    @JsonProperty("deadlines")
    private Set<Deadline> deadlines;

}

