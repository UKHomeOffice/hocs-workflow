package uk.gov.digital.ho.hocs.workflow.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;

@ControllerAdvice
@Slf4j
public class RestResponseSecurityExceptionHandler {

    @ExceptionHandler(SecurityExceptions.PermissionCheckException.class)
    public ResponseEntity handle(SecurityExceptions.PermissionCheckException e) {
        log.error("SecurityException: {}", e.getMessage(), value(EVENT,e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), UNAUTHORIZED);
    }

    @ExceptionHandler(SecurityExceptions.StageNotAssignedToLoggedInUserException.class)
    public ResponseEntity handle(SecurityExceptions.StageNotAssignedToLoggedInUserException e) {
        log.error("SecurityException: {}", e.getMessage(), value(EVENT, e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
    }

    @ExceptionHandler(SecurityExceptions.StageNotAssignedToUserTeamException.class)
    public ResponseEntity handle(SecurityExceptions.StageNotAssignedToUserTeamException e) {
        log.error("SecurityException: {}", e.getMessage(), value(EVENT, e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
    }
}
