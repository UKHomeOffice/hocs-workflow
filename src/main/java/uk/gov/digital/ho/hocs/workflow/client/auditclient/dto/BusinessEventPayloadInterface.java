package uk.gov.digital.ho.hocs.workflow.client.auditclient.dto;

import uk.gov.digital.ho.hocs.workflow.client.auditclient.EventType;

public interface BusinessEventPayloadInterface {
    String getName();

    EventType getEventType();
}
