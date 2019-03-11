package uk.gov.digital.ho.hocs.workflow.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;

import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
@Profile("!migration")
@Order(value = 2)
public class AllocatedAspect {

    private CaseworkClient caseworkClient;
    private UserPermissionsService userService;

    @Around("@annotation(allocated)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Allocated allocated) throws Throwable {
        UUID caseUUID;
        UUID stageUUID;
        if (joinPoint.getArgs().length >= 2) {
            if (joinPoint.getArgs()[0] instanceof UUID && joinPoint.getArgs()[1] instanceof UUID) {
                caseUUID = (UUID) joinPoint.getArgs()[0];
                stageUUID = (UUID) joinPoint.getArgs()[1];
            } else {
                throw new SecurityExceptions.PermissionCheckException("Unable parse method parameters for type " + joinPoint.getArgs()[1].getClass().getName(), SECURITY_PARSE_ERROR);
            }
        } else {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without stage UUID parameter", SECURITY_PARSE_ERROR);
        }

        log.info("Checking allocation permissions for user {} and case type {}", userService.getUserId().toString(), caseUUID.toString());

        switch (allocated.allocatedTo()) {
            case USER:
                UUID userId = userService.getUserId();
                UUID assignedUser = caseworkClient.getStageUser(caseUUID, stageUUID);
                if (!userId.equals(assignedUser)) {
                    throw new SecurityExceptions.StageNotAssignedToLoggedInUserException("Stage " + stageUUID.toString() + " is assigned to " + assignedUser, SECURITY_CASE_NOT_ALLOCATED_TO_USER);
                }
                break;
            case TEAM:
                Set<UUID> teams = userService.getUserTeams();
                UUID assignedTeam = caseworkClient.getStageTeam(caseUUID, stageUUID);
                if (!teams.contains(assignedTeam)) {
                    throw new SecurityExceptions.StageNotAssignedToUserTeamException("Stage " + stageUUID.toString() + " is assigned to " + assignedTeam, SECURITY_CASE_NOT_ALLOCATED_TO_TEAM);
                }
                break;
            default:
                throw new SecurityExceptions.PermissionCheckException("Invalid Allocation type", SECURITY_PARSE_ERROR);
        }

        return joinPoint.proceed();

    }
}

