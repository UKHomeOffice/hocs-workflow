package uk.gov.digital.ho.hocs.workflow.domain.exception;

import uk.gov.digital.ho.hocs.workflow.application.LogEvent;

public interface ApplicationExceptions {
    class EntityCreationException extends RuntimeException {
        public EntityCreationException(String msg, Object... args) {
            super(String.format(msg, args));
        }
    }

    class EntityNotFoundException extends RuntimeException {
        public EntityNotFoundException(String msg, Object... args) {
            super(String.format(msg, args));
        }
    }

    class ClientException extends RuntimeException {
        public ClientException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
        }
    }
}