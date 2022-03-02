package uk.gov.digital.ho.hocs.workflow.security;

import uk.gov.digital.ho.hocs.workflow.application.LogEvent;

public interface SecurityExceptions {
    class StageNotAssignedToLoggedInUserException extends RuntimeException {
        final LogEvent event;
        StageNotAssignedToLoggedInUserException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }

    class StageNotAssignedToUserTeamException extends RuntimeException {
        final LogEvent event;
        StageNotAssignedToUserTeamException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }

    class PermissionCheckException extends RuntimeException {
        final LogEvent event;
        PermissionCheckException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }
        public LogEvent getEvent() {return event;}
    }

    class AuthFilterException extends RuntimeException {
        final LogEvent event;

        public AuthFilterException(String s, LogEvent event) {
            super(s);
            this.event = event;
        }

        public LogEvent getEvent() {return event;}
    }
}
