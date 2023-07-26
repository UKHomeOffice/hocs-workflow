package uk.gov.digital.ho.hocs.workflow.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseRequestInterface;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Aspect
@Component
public class AuthorisationAspect {

    private final InfoClient infoClient;

    private final UserPermissionsService userService;

    public AuthorisationAspect(InfoClient infoClient,
                               UserPermissionsService userService) {
        this.infoClient = infoClient;
        this.userService = userService;
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {

        AccessLevel userLevel = getUserAccessLevel(joinPoint);

        if (isSufficientLevel(userLevel.getLevel(), authorised)) {
            return joinPoint.proceed();
        }

        throw new SecurityExceptions.PermissionCheckException("User does not have access to the requested resource",
            SECURITY_UNAUTHORISED);
    }

    private boolean isSufficientLevel(int userLevelAsInt, Authorised authorised) {
        return userLevelAsInt >= getRequiredAccessLevel(authorised).getLevel();
    }

    AccessLevel getAccessRequestAccessLevel() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes == null) {
            throw new SecurityExceptions.PermissionCheckException("Null Request attributes ", SECURITY_PARSE_ERROR);
        }

        var method = requestAttributes.getRequest().getMethod();
        switch (method) {
            case "GET":
                return AccessLevel.SUMMARY;
            case "POST":
            case "PUT":
            case "DELETE":
            case "PATCH":
                return AccessLevel.WRITE;
            default:
                throw new SecurityExceptions.PermissionCheckException("Unknown access request type " + method,
                    SECURITY_PARSE_ERROR);
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
        if (joinPoint.getArgs().length == 0) {
            throw new SecurityExceptions.PermissionCheckException(
                "Unable to check permission of method without case UUID parameters", SECURITY_PARSE_ERROR);
        }

        String caseType;
        if (joinPoint.getArgs()[0] instanceof UUID caseUUID) {
            String shortCode = caseUUID.toString().substring(34);
            caseType = infoClient.getCaseTypeByShortCode(shortCode).getDisplayCode();
        } else if (joinPoint.getArgs()[0] instanceof CreateCaseRequestInterface createCaseRequest) {
            caseType = createCaseRequest.getType();
        } else {
            throw new SecurityExceptions.PermissionCheckException(
                "Unable parse method parameters (access level) for type " + joinPoint.getArgs()[0].getClass().getName(),
                SECURITY_PARSE_ERROR);
        }
        return userService.getMaxAccessLevel(caseType);
    }

}
