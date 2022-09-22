package uk.gov.digital.ho.hocs.workflow.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;

@ControllerAdvice
@Slf4j
@Order(HIGHEST_PRECEDENCE)
public class RestResponseSecurityExceptionHandler {

    @ExceptionHandler(SecurityExceptions.PermissionCheckException.class)
    public ResponseEntity<String> handle(SecurityExceptions.PermissionCheckException e) {
        log.error("SecurityException: {} {}", e.getMessage(), value(EVENT, e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), UNAUTHORIZED);
    }

    @ExceptionHandler(SecurityExceptions.StageNotAssignedToLoggedInUserException.class)
    public ResponseEntity<String> handle(SecurityExceptions.StageNotAssignedToLoggedInUserException e) {
        // SECURITY_CASE_NOT_ALLOCATED_TO_USER is misused by the frontend to drive its logic.
        // The original intent of the message has been lost. Lowering to WARN, so that real errors can be seen
        if (e.getEvent().equals(LogEvent.SECURITY_CASE_NOT_ALLOCATED_TO_USER)) {
            log.warn("SecurityException: {} {}", e.getMessage(), value(EVENT, e.getEvent()));
        } else {
            log.error("SecurityException: {} {}", e.getMessage(), value(EVENT, e.getEvent()));
        }
        return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
    }

    @ExceptionHandler(SecurityExceptions.StageNotAssignedToUserTeamException.class)
    public ResponseEntity<String> handle(SecurityExceptions.StageNotAssignedToUserTeamException e) {
        if (e.getEvent().equals(LogEvent.SECURITY_CASE_NOT_ALLOCATED_TO_TEAM)) {
            // SECURITY_CASE_NOT_ALLOCATED_TO_TEAM is misused by the frontend to drive its logic.
            // The original intent of the message has been lost. Lowering to WARN, so that real errors can be seen
            log.warn("SecurityException: {} {}", e.getMessage(), value(EVENT, e.getEvent()));
        } else {
            log.error("SecurityException: {} {}", e.getMessage(), value(EVENT, e.getEvent()));
        }
        return new ResponseEntity<>(e.getMessage(), UNAUTHORIZED);
    }

}
