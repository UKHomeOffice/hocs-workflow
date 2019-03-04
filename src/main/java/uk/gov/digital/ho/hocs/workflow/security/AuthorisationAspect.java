package uk.gov.digital.ho.hocs.workflow.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.SECURITY_PARSE_ERROR;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.SECURITY_UNAUTHORISED;

@Aspect
@Component
@Slf4j
@Profile("!migration")
public class AuthorisationAspect {

    private CaseworkClient caseworkClient;
    private UserPermissionsService userService;

    public AuthorisationAspect(CaseworkClient caseworkClient, UserPermissionsService userService) {
        this.caseworkClient = caseworkClient;
        this.userService = userService;
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {

        if (getUserAccessLevel(joinPoint).getLevel() >= getRequiredAccessLevel(authorised).getLevel()) {
            return joinPoint.proceed();
        } else {
            throw new SecurityExceptions.PermissionCheckException("User does not have access to the requested resource", SECURITY_UNAUTHORISED);
        }
    }

    AccessLevel getAccessRequestAccessLevel() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String method = requestAttributes.getRequest().getMethod();
        switch (method) {
            case "GET":
                return AccessLevel.SUMMARY;
            case "POST":
            case "PUT":
            case "DELETE":
                return AccessLevel.WRITE;
            default:
                throw new SecurityExceptions.PermissionCheckException("Unknown access request type " + method, SECURITY_PARSE_ERROR);
        }
    }

    private AccessLevel getRequiredAccessLevel(Authorised authorised) {
        if (authorised.accessLevel() != AccessLevel.UNSET) {
            return authorised.accessLevel();
        } else {
            return getAccessRequestAccessLevel();
        }
    }

    private AccessLevel getUserAccessLevel(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getArgs().length > 0) {
            String caseType;
            if (joinPoint.getArgs()[0] instanceof UUID) {
                UUID caseUUID = (UUID) joinPoint.getArgs()[0];
                caseType = caseworkClient.getCaseType(caseUUID);
            } else if (joinPoint.getArgs()[0] instanceof CreateCaseRequest) {
                CreateCaseRequest createCaseRequest = (CreateCaseRequest) joinPoint.getArgs()[0];
                caseType = createCaseRequest.getType().getType();
            } else {
                throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[0].getClass().getName(), SECURITY_PARSE_ERROR);
            }
            return userService.getMaxAccessLevel(caseType);
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without case UUID parameters", SECURITY_PARSE_ERROR);
        }
    }
}