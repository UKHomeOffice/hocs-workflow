package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
public class GetCaseTopicsResponse {

    @JsonProperty("topics")
    private Set<Topic> topics;

}
