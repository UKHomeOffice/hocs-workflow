package uk.gov.digital.ho.hocs.workflow.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllocatedAspectTest {

    @Mock
    private CaseworkClient caseworkClient;

    @Mock
    private UserPermissionsService userService;

    @Mock
    Allocated annotation;

    private AllocatedAspect aspect;

    private UUID stageUUID = UUID.randomUUID();
    private UUID caseUUID = UUID.randomUUID();

    private UUID userId = UUID.randomUUID();
    private UUID teamId = UUID.randomUUID();

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Before
    public void setup() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void shouldCallCollaboratorsForUser() throws Throwable {
        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;
        when(caseworkClient.getStageUser(caseUUID, stageUUID)).thenReturn(userId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getUserId()).thenReturn(userId);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);

        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(caseworkClient, times(1)).getStageUser(caseUUID, stageUUID);
        verify(userService, times(2)).getUserId();
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }

    @Test
    public void shouldCallCollaboratorsForTeam() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getUserTeams()).thenReturn(new HashSet<>(Arrays.asList(teamId)));
        when(userService.getUserId()).thenReturn(userId);
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(userService, times(1)).getUserTeams();
        verify(caseworkClient, times(1)).getStageTeam(caseUUID,stageUUID);
        verify(userService, times(1)).getUserId();
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }

    @Test
    public void shouldProceedIfUserIsAllocatedToCase() throws Throwable {
        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;
        when(caseworkClient.getStageUser(caseUUID, stageUUID)).thenReturn(userId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getUserId()).thenReturn(userId);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, times(1)).proceed();

    }

    @Test
    public void shouldProceedIfTeamIsAllocatedToCase() throws Throwable {

        Object[] args = new Object[2];
        args[0] = caseUUID;
        args[1] = stageUUID;
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getUserTeams()).thenReturn(new HashSet<>(Arrays.asList(teamId)));
        when(userService.getUserId()).thenReturn(userId);
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, times(1)).proceed();

    }


    @Test
    public void shouldProceedIfUserAndTeamIsAllocatedToCase() throws Throwable {

        Object[] args = new Object[2];
        args[0] = caseUUID;
        args[1] = stageUUID;
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamId);;
        when(caseworkClient.getStageUser(caseUUID, stageUUID)).thenReturn(userId);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM_USER);
        when(userService.getUserTeams()).thenReturn(new HashSet<>(Arrays.asList(teamId)));
        when(userService.getUserId()).thenReturn(userId);
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, times(1)).proceed();

    }


    @Test(expected = SecurityExceptions.StageNotAssignedToLoggedInUserException.class)
    public void shouldNotProceedIfTeamHasPermissionButUserDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamId);;
        when(caseworkClient.getStageUser(caseUUID, stageUUID)).thenReturn(userId);
        when(userService.getUserId()).thenReturn(UUID.randomUUID());
        when(userService.getUserTeams()).thenReturn(new HashSet<>(Arrays.asList(teamId)));
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM_USER);
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = SecurityExceptions.StageNotAssignedToUserTeamException.class)
    public void shouldNotProceedIfUserHasPermissionButTeamDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM_USER);
        when(userService.getUserTeams()).thenReturn(new HashSet<UUID>(){{UUID.randomUUID();}});
        when(userService.getUserId()).thenReturn(userId);
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamId);;
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = SecurityExceptions.StageNotAssignedToLoggedInUserException.class)
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getUserId()).thenReturn(UUID.randomUUID());
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.USER);
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = SecurityExceptions.StageNotAssignedToUserTeamException.class)
    public void shouldNotProceedIfTeamDoesNotHavePermission() throws Throwable {

        Object[] args = new Object[2];
        args[1] = stageUUID;
        args[0] = caseUUID;

        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.allocatedTo()).thenReturn(AllocationLevel.TEAM);
        when(userService.getUserTeams()).thenReturn(new HashSet<UUID>(){{UUID.randomUUID();}});
        when(userService.getUserId()).thenReturn(userId);
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }

    @Test(expected = SecurityExceptions.PermissionCheckException.class)
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[2];
        args[1] = "bad UUID";
        args[0] = caseUUID;
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        aspect = new AllocatedAspect(caseworkClient,userService);
        aspect.validateUserAccess(proceedingJoinPoint, annotation);
        verify(proceedingJoinPoint, never()).proceed();

    }


}