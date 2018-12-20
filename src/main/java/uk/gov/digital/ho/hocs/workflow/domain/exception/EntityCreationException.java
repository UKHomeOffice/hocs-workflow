package uk.gov.digital.ho.hocs.workflow.domain.exception;

import uk.gov.digital.ho.hocs.workflow.application.LogEvent;

public class EntityCreationException extends RuntimeException {

    public EntityCreationException(String msg) {
        super(msg);
    }
    LogEvent event;
    public EntityCreationException(String msg, LogEvent event, Object... args) {
        super(String.format(msg, args));
        this.event = event;
    }

    public LogEvent getEvent() {
        return event;
    }
}
