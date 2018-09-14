package uk.gov.digital.ho.hocs.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor()
@Getter
public class GetParentTopicResponse {

    @JsonProperty("topics")
    private final List<Topic> topics;

}
