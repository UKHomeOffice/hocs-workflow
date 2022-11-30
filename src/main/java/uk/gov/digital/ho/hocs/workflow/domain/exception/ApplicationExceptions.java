package uk.gov.digital.ho.hocs.workflow.domain.exception;

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

    class InvalidMethodArgumentException extends RuntimeException {

        public InvalidMethodArgumentException(String msg) {
            super(msg);

        }

    }

    class ScreenNotFoundException extends RuntimeException {

        private final LogEvent event;

        public ScreenNotFoundException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }

    }

    class JsonFileReadException extends RuntimeException {

        private final LogEvent event;

        public JsonFileReadException(String msg, LogEvent event) {
            super(msg);
            this.event = event;
        }

        public LogEvent getEvent() {
            return event;
        }

    }

}
