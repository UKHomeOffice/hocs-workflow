package uk.gov.digital.ho.hocs.workflow.security;

public interface SecurityExceptions {
    class StageNotAssignedToLoggedInUserException extends RuntimeException {
        public StageNotAssignedToLoggedInUserException(String s) {
            super(s);
        }
    }

    class StageNotAssignedToUserTeamException extends RuntimeException {
        public StageNotAssignedToUserTeamException(String s) {
            super(s);
        }
    }

    class PermissionCheckException extends RuntimeException {
        public PermissionCheckException(String s) {
            super(s);
        }
    }


}
