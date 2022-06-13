package uk.gov.digital.ho.hocs.workflow.security;

import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;

import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

@Aspect
@Component
@AllArgsConstructor
public class AllocatedAspect {

    private CaseworkClient caseworkClient;
    private InfoClient infoClient;
    private UserPermissionsService userService;

    @Around("@annotation(allocated)")
    public Object validateUserAccess(ProceedingJoinPoint joinPoint, Allocated allocated) throws Throwable {
        if (joinPoint.getArgs().length < 2) {
            throw new SecurityExceptions.PermissionCheckException("Unable to check permission of method without stage UUID parameter", SECURITY_PARSE_ERROR);
        }

        if (!(joinPoint.getArgs()[0] instanceof UUID caseUUID &&
                joinPoint.getArgs()[1] instanceof UUID stageUUID)) {
            throw new SecurityExceptions.PermissionCheckException(
                    "Unable parse method parameters (allocated) for types ["
                    + joinPoint.getArgs()[0].getClass().getName() + ","
                    + joinPoint.getArgs()[1].getClass().getName() + "]"
                    , SECURITY_PARSE_ERROR
            );
        }

        if (proceedIfUserTeamIsAdminForCaseType(caseUUID)) {
            return joinPoint.proceed();
        }

        switch (allocated.allocatedTo()) {
            case USER -> verifyAllocatedToUser(caseUUID, stageUUID);
            case TEAM -> verifyAllocatedToTeam(caseUUID, stageUUID);
            case TEAM_USER -> {
                verifyAllocatedToTeam(caseUUID, stageUUID);
                verifyAllocatedToUser(caseUUID, stageUUID);
            }
            default -> throw new SecurityExceptions.PermissionCheckException("Invalid Allocation type", SECURITY_PARSE_ERROR);
        }

        return joinPoint.proceed();

    }

    private void verifyAllocatedToUser(UUID caseUUID, UUID stageUUID) {
        UUID userId = userService.getUserId();
        UUID assignedUser = caseworkClient.getStageUser(caseUUID, stageUUID);
        if (!userId.equals(assignedUser)) {
            throw new SecurityExceptions.StageNotAssignedToLoggedInUserException("Stage " + stageUUID.toString() + " is assigned to " + assignedUser, SECURITY_CASE_NOT_ALLOCATED_TO_USER);
        }
    }

    private void verifyAllocatedToTeam(UUID caseUUID, UUID stageUUID) {
        Set<UUID> teams = userService.getUserTeams();
        UUID assignedTeam = caseworkClient.getStageTeam(caseUUID, stageUUID);
        if (!teams.contains(assignedTeam)) {
            throw new SecurityExceptions.StageNotAssignedToUserTeamException("Stage " + stageUUID.toString() + " is assigned to " + assignedTeam, SECURITY_CASE_NOT_ALLOCATED_TO_TEAM);
        }
    }

    private boolean proceedIfUserTeamIsAdminForCaseType(UUID caseUUID) {
        String shortCode = caseUUID.toString().substring(34);
        var caseDatatype = infoClient.getCaseTypeByShortCode(shortCode);
        if(caseDatatype == null) {
            return false;
        }
        Set<String> caseTypesForCaseTypeAdmin = userService.getCaseTypesIfUserTeamIsCaseTypeAdmin();
        return caseTypesForCaseTypeAdmin.contains(caseDatatype.getDisplayCode());
    }
}
