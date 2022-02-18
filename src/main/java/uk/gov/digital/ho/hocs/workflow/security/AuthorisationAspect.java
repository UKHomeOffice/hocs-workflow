package uk.gov.digital.ho.hocs.workflow.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseRequestInterface;
import uk.gov.digital.ho.hocs.workflow.application.NonMigrationEnvCondition;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.security.filters.AuthFilter;

import java.util.*;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Aspect
@Component
@Slf4j
@Conditional(value = {NonMigrationEnvCondition.class})
public class AuthorisationAspect {

    private InfoClient infoClient;
    private UserPermissionsService userService;
    private final Map<String,AuthFilter> authFilterList = new HashMap<>();


    public AuthorisationAspect(InfoClient infoClient, UserPermissionsService userService, List<AuthFilter> authFilters) {
        this.infoClient = infoClient;
        this.userService = userService;
        authFilters.forEach(filter -> authFilterList.put(filter.getKey(), filter));
    }

    @Around("@annotation(authorised)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Authorised authorised) throws Throwable {
        if (isAllowedToProceed(joinPoint, authorised)) {

            Object response = joinPoint.proceed();
            return filterResponseByPermissionLevel(response, getUserAccessLevel(joinPoint));
        } else {
            throw new SecurityExceptions.PermissionCheckException("User does not have access to the requested resource", SECURITY_UNAUTHORISED);
        }
    }

    private boolean isAllowedToProceed(ProceedingJoinPoint joinPoint, Authorised authorised) {
        return (getUserAccessLevel(joinPoint).getLevel() >= getRequiredAccessLevel(authorised).getLevel()) || isPermittedLowerLevel(authorised,getUserAccessLevel(joinPoint).getLevel());
    }

    private boolean isPermittedLowerLevel(Authorised authorised, int usersLevel) {
        return Arrays.stream(authorised.permittedLowerLevels()).anyMatch(level -> level.getLevel() == usersLevel);
    }

    private Object filterResponseByPermissionLevel(Object objectToFilter, AccessLevel userAccessLevel) throws Exception {
        log.debug("Checking if response filtering is required");

        if (objectToFilter.getClass() != ResponseEntity.class) {
            return objectToFilter;
        }

        ResponseEntity<?> responseEntityToFilter = (ResponseEntity<?>) objectToFilter;

        String simpleName;
        Object object = responseEntityToFilter.getBody();
        Object[] collectionAsArray = new Object[0];

        if (responseEntityToFilter.getBody() != null && object != null) {

            if (!Collection.class.isAssignableFrom(object.getClass())) {
                simpleName = object.getClass().getSimpleName();
            } else {
                collectionAsArray = ((Collection<?>) object).toArray();
                if (collectionAsArray.length < 1) {
                    log.trace("No elements in array, cannot get filter key, setting key to NONE");
                    simpleName =  "NONE";
                } else {
                    simpleName= collectionAsArray[0].getClass().getSimpleName();
                }
            }

            AuthFilter filter = authFilterList.get(simpleName);

            if (filter != null) {
                log.info("Filtering response for {} call.", simpleName, value(EVENT, AUTH_FILTER_SUCCESS));
                return filter.applyFilter(responseEntityToFilter, userAccessLevel, collectionAsArray);
            }
        }
        log.trace("The response does not need to be filtered.");
        return objectToFilter;

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
                log.info("Checking authorisation permissions for user {} and case type {}", userService.getUserId().toString(), caseUUID.toString());
                String shortCode = caseUUID.toString().substring(34);
                caseType = infoClient.getCaseTypeByShortCode(shortCode).getDisplayCode();
            } else if (joinPoint.getArgs()[0] instanceof CreateCaseRequestInterface) {
                CreateCaseRequestInterface createCaseRequest = (CreateCaseRequestInterface) joinPoint.getArgs()[0];
                caseType = createCaseRequest.getType();
            } else {
                throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters (access level) for type " + joinPoint.getArgs()[0].getClass().getName(), SECURITY_PARSE_ERROR);
            }
            return userService.getMaxAccessLevel(caseType);
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without case UUID parameters", SECURITY_PARSE_ERROR);
        }
    }
}