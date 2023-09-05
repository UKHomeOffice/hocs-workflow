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
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationAspectTest {

    @Mock
    private UserPermissionsService userService;

    @Mock
    private InfoClient infoClient;

    @Mock
    private Authorised annotation;

    private AuthorisationAspect aspect;

    private UUID caseUUID = UUID.randomUUID();

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Before
    public void setup() {

        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        aspect = new AuthorisationAspect(infoClient, userService);
    }

    @Test
    public void shouldCaseServiceLookupWhenExistingCase() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(infoClient, times(1)).getCaseTypeByShortCode(caseUUID.toString().substring(34));
        verify(userService, times(1)).getMaxAccessLevel(type);
        verify(proceedingJoinPoint, atLeast(1)).getArgs();

        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldNotCallCaseServiceWhenNewCase() throws Throwable {
        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = new CreateCaseRequest("MIN", LocalDate.now(), new HashMap<>(), new ArrayList<>(), null);

        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(infoClient, never()).getCaseTypeByShortCode(caseUUID.toString().substring(34));
        verify(userService, times(1)).getMaxAccessLevel(type);
        verify(proceedingJoinPoint, atLeast(1)).getArgs();
    }

    @Test
    public void shouldProceedIfUserHasPermission() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(proceedingJoinPoint, times(1)).proceed();
        verify(infoClient, times(1)).getCaseTypeByShortCode(caseUUID.toString().substring(34));

        verifyNoMoreInteractions(infoClient);
    }

    @Test(expected = SecurityExceptions.PermissionCheckException.class)
    public void shouldNotProceedIfUserDoesNotHavePermission() throws Throwable {

        String type = "MIN";
        Object[] args = new Object[1];
        args[0] = caseUUID;
        when(userService.getMaxAccessLevel(any())).thenThrow(
            new SecurityExceptions.PermissionCheckException("User does not have any permission for this case type",
                LogEvent.SECURITY_UNAUTHORISED));
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(proceedingJoinPoint, never()).proceed();
        verify(infoClient, times(1)).getCaseTypeByShortCode(caseUUID.toString().substring(34));

        verifyNoMoreInteractions(infoClient);
    }

    @Test(expected = SecurityExceptions.PermissionCheckException.class)
    public void shouldThrowExceptionOnError() throws Throwable {

        Object[] args = new Object[1];
        args[0] = "bad UUID";
        when(proceedingJoinPoint.getArgs()).thenReturn(args);

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

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
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.READ);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(annotation, times(2)).accessLevel();
        verify(infoClient, times(1)).getCaseTypeByShortCode(caseUUID.toString().substring(34));

        verifyNoMoreInteractions(infoClient);
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
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(annotation.accessLevel()).thenReturn(AccessLevel.UNSET);
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        verify(annotation, times(1)).accessLevel();
        verify(infoClient, times(1)).getCaseTypeByShortCode(caseUUID.toString().substring(34));

        verifyNoMoreInteractions(infoClient);

    }
}
