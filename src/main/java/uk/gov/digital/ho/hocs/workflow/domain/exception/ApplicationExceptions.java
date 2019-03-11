package uk.gov.digital.ho.hocs.workflow.domain.exception;

import org.springframework.http.HttpStatus;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;

public interface ApplicationExceptions {
    class EntityCreationException extends RuntimeException {
        private final LogEvent event;

        public EntityCreationException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }
    }

    class EntityNotFoundException extends RuntimeException {
        private final LogEvent event;

        public EntityNotFoundException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }
    }

    class ClientException extends RuntimeException {
        private final LogEvent event;

        public ClientException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }
    }

    class ResourceException extends RuntimeException {
        private final LogEvent event;

        ResourceException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }
    }

    class CaseworkException extends RuntimeException {
        private final LogEvent event;
        private final HttpStatus statusCode;
        public CaseworkException(String msg, HttpStatus statusCode, LogEvent event) {
            super(msg);
            this.event = event;
            this.statusCode = statusCode;
        }
        public LogEvent getEvent() {
            return event;
        }

        public HttpStatus getStatusCode() { return statusCode; }
    }
}