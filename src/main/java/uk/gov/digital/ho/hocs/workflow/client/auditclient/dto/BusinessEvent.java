package uk.gov.digital.ho.hocs.workflow.client.auditclient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.EventType;

@AllArgsConstructor
@Getter
public class BusinessEvent {
    private EventType event;
    private String message;
}
