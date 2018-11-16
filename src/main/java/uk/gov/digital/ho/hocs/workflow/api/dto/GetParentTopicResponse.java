package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor()
@NoArgsConstructor
@Getter
public class GetParentTopicResponse {

    @JsonProperty("parentTopics")
    private List<ParentTopicAndTopics> parentTopics;

}
