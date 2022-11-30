package uk.gov.digital.ho.hocs.workflow.client.auditclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.EventType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class DataFieldUpdatedPayload implements BusinessEventPayloadInterface {

    @JsonProperty("name")
    private String name;

    @JsonProperty("fieldName")
    private String fieldName;

    public EventType getEventType() {
        return EventType.DATA_FIELD_UPDATED;
    };

    @JsonProperty("newValue")
    private String newValue;

    @JsonProperty("previousValue")
    private String previousValue;

}
