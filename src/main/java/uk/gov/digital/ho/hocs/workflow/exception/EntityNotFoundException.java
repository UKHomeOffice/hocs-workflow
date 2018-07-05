package uk.gov.digital.ho.hocs.workflow.exception;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityNotFoundException extends Exception {

    public EntityNotFoundException(String msg) {
        super(msg);
    }

    public EntityNotFoundException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
