package uk.gov.digital.ho.hocs.workflow.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseRequestInterface;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.security.filters.AuthFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Aspect
@Component
public class AuthorisationAspect {

    private final InfoClient infoClient;
    private final UserPermissionsService userService;
    private final Map<String, AuthFilter> authFilterMap = new HashMap<>();

    public AuthorisationAspect(InfoClient infoClient, UserPermissionsService userService, List<AuthFilter> authFilters) {
        this.infoClient = infoClient;
        this.userService = userService;
        authFilters.forEach(filter -> authFilterMap.put(filter.getKey(), filter));
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {

        AccessLevel userLevel = getUserAccessLevel(joinPoint);

        if(isSufficientLevel(userLevel.getLevel(), authorised)) {
            return joinPoint.proceed();
        }

        if (isPermittedLowerLevel(userLevel.getLevel(), authorised)) {
            return filterResponseByPermissionLevel(joinPoint.proceed(), userLevel);
        }

        throw new SecurityExceptions.PermissionCheckException("User does not have access to the requested resource", SECURITY_UNAUTHORISED);
    }

    private boolean isSufficientLevel(int userLevelAsInt, Authorised authorised) {
        return userLevelAsInt >= getRequiredAccessLevel(authorised).getLevel();
    }

    private boolean isPermittedLowerLevel( int usersLevel, Authorised authorised) {
        return Arrays.stream(authorised.permittedLowerLevels()).anyMatch(level -> level.getLevel() == usersLevel);
    }

    private Object filterResponseByPermissionLevel(Object objectToFilter, AccessLevel userAccessLevel) throws SecurityExceptions.AuthFilterException {

        if (objectToFilter.getClass() != ResponseEntity.class) {
             return objectToFilter;
        }

        ResponseEntity<?> responseEntityToFilter = (ResponseEntity<?>) objectToFilter;

        String simpleName = "DEFAULT";
        Object object = responseEntityToFilter.getBody();
        Object[] collectionAsArray = new Object[0];

        if (object != null && !Collection.class.isAssignableFrom(object.getClass())) {
            simpleName = object.getClass().getSimpleName();
        }

        if (object != null && Collection.class.isAssignableFrom(object.getClass())) {
            collectionAsArray = ((Collection<?>) object).toArray();
            simpleName = collectionAsArray.length > 0 ? collectionAsArray[0].getClass().getSimpleName() : simpleName;
        }

        AuthFilter filter = authFilterMap.get(simpleName);

        if (filter != null) {
            return filter.applyFilter(responseEntityToFilter, userAccessLevel, collectionAsArray);
        }

        return objectToFilter;
    }

    AccessLevel getAccessRequestAccessLevel() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if(requestAttributes == null) {
            throw new SecurityExceptions.PermissionCheckException("Null Request attributes ", SECURITY_PARSE_ERROR);
        }

        var method = requestAttributes.getRequest().getMethod();
        return switch (method) {
            case "GET" -> AccessLevel.SUMMARY;
            case "POST", "PUT", "DELETE", "PATCH" -> AccessLevel.WRITE;
            default -> throw new SecurityExceptions.PermissionCheckException("Unknown access request type " + method, SECURITY_PARSE_ERROR);
        };
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
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without case UUID parameters", SECURITY_PARSE_ERROR);
        }

        String caseType;
        if (joinPoint.getArgs()[0] instanceof UUID caseUUID) {
            String shortCode = caseUUID.toString().substring(34);
            caseType = infoClient.getCaseTypeByShortCode(shortCode).getDisplayCode();
        } else if (joinPoint.getArgs()[0] instanceof CreateCaseRequestInterface createCaseRequest) {
            caseType = createCaseRequest.getType();
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters (access level) for type " + joinPoint.getArgs()[0].getClass().getName(), SECURITY_PARSE_ERROR);
        }
        return userService.getMaxAccessLevel(caseType);
    }
}
