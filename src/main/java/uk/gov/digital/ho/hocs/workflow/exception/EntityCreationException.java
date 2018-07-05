package uk.gov.digital.ho.hocs.workflow.exception;

public class EntityCreationException extends Exception {

    public EntityCreationException(String msg) {
        super(msg);
    }

    public EntityCreationException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
