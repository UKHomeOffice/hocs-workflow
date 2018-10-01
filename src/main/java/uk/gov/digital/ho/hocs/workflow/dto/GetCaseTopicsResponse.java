package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
public class GetCaseTopicsResponse {

    @JsonProperty("topics")
    private Set<Topic> topics;

}
