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
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationAspectTest {

    @Mock
    private UserPermissionsService userService;

    @Mock
    private CaseworkClient caseService;

    @Mock
    private Authorised annotation;

    private AuthorisationAspect aspect;

    private UUID caseUUID = UUID.randomUUID();

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Before
    public void setup() {
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        when(userService.getUserId()).thenReturn(UUID.randomUUID());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        aspect = new AuthorisationAspect(caseService, userService);

    }

    @Test
    public void shouldCaseServiceLookupWhenExistingCase() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(caseService.getCaseType(caseUUID)).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(caseService, times(1)).getCaseType(caseUUID);
        verify(userService, times(1)).getMaxAccessLevel(type);
        verify(proceedingJoinPoint, atLeast(1)).getArgs();

        verifyNoMoreInteractions(caseService);
    }

    //todo: fallback to checking database tests.

    @Test
    public void shouldNotCallCaseServiceWhenNewCase() throws Throwable {
        String type = "MIN";
        Object[] args = new Object[1];
        //args[0] = new CreateCaseRequest(new CaseDataType.(type, null), new HashMap<>());
        args[0] = new CreateCaseRequest(CaseDataType.MIN, LocalDate.now(), new ArrayList<>());
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(caseService, never()).getCase(caseUUID);
        verify(userService, times(1)).getMaxAccessLevel(type);
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }


    @Test
    public void shouldProceedIfUserHasPermission() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(caseService, times(1)).getCaseType(caseUUID);

        verifyNoMoreInteractions(caseService);
    }

    @Test(expected = SecurityExceptions.PermissionCheckException.class)
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(userService.getMaxAccessLevel(any())).thenThrow(new SecurityExceptions.PermissionCheckException("User does not have any permission for this case type", LogEvent.SECURITY_UNAUTHORISED));
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(proceedingJoinPoint, never()).proceed();
        verify(caseService, times(1)).getCaseType(caseUUID);

        verifyNoMoreInteractions(caseService);
    }

    @Test(expected = SecurityExceptions.PermissionCheckException.class)
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[1];
        args[0] = "bad UUID";
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(proceedingJoinPoint, never()).proceed();
    }


    @Test
    public void shouldGetRequestedPermissionTypeFromRequestWhenAnnotationIsNull() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertThat(aspect.getAccessRequestAccessLevel()).isEqualTo(AccessLevel.SUMMARY);
    }

    @Test
    public void shouldGetRequestedPermissionTypeFromRequestWhenAnnotationIsSet() throws Throwable {
        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(annotation, times(2)).accessLevel();
        verify(caseService, times(1)).getCaseType(caseUUID);

        verifyNoMoreInteractions(caseService);

    }

    @Test
    public void shouldGetRequestedPermissionTypeFromRequestWhenUNSET() throws Throwable {
        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        when(caseService.getCaseType(any())).thenReturn(type);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.UNSET);

        aspect.validateUserAccess(proceedingJoinPoint,annotation);

        verify(annotation, times(1)).accessLevel();
        verify(caseService, times(1)).getCaseType(caseUUID);

        verifyNoMoreInteractions(caseService);

    }

}