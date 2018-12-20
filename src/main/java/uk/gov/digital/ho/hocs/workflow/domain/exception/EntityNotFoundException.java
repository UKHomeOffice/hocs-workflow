package uk.gov.digital.ho.hocs.workflow.domain.exception;

import uk.gov.digital.ho.hocs.workflow.application.LogEvent;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String msg, LogEvent event) {
        super(msg);
    }

    LogEvent event;
    public EntityNotFoundException(String msg, LogEvent event, Object... args) {
        super(String.format(msg, args));
        this.event = event;
    }

    public LogEvent getEvent() {
        return event;
    }
}
