package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
public class ParentTopicAndTopics {

    @JsonProperty("label")
    private String displayName;

    @JsonProperty("value")
    private UUID uuid;

    @JsonProperty("options")
    List<Topic> topics;

}