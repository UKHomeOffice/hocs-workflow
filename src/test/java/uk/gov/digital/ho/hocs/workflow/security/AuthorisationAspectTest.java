package uk.gov.digital.ho.hocs.workflow.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.security.filters.AuthFilter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
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

    private final List<AuthFilter> authFilterList = new ArrayList<>();

    @Spy
    private AuthFilter testAuthFilter = new TestAuthFilter();

    @Before
    public void setup() {

        when(userService.getMaxAccessLevel(any())).thenReturn(AccessLevel.OWNER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        authFilterList.add(testAuthFilter);
        aspect = new AuthorisationAspect(infoClient, userService, authFilterList);
    }

    @Test
    public void testShouldLoadFilter() {
        // GIVEN
        // WHEN - class loaded in setup method.

        // THEN
        verify(testAuthFilter, times(1)).getKey();
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

    @Test(expected = SecurityExceptions.PermissionCheckException.class)
    public void testShouldRejectWhenUserLevelIsBelowRequiredAndPermittedLower() throws Throwable {

        // GIVEN
        String type = "ANY";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getMaxAccessLevel(type)).thenReturn(AccessLevel.READ);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(annotation.permittedLowerLevels()).thenReturn(new AccessLevel[] { AccessLevel.RESTRICTED_OWNER });

        // WHEN
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        // THEN - expect exception

    }

    @Test
    public void testShouldAllowWhenUserLevelIsBelowRequiredButIsPermittedLower() throws Throwable {

        // GIVEN
        String type = "ANY";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getMaxAccessLevel(type)).thenReturn(AccessLevel.RESTRICTED_OWNER);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(annotation.permittedLowerLevels()).thenReturn(new AccessLevel[] { AccessLevel.RESTRICTED_OWNER });
        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        // WHEN
        // THEN
        assertThatNoException().isThrownBy(() -> aspect.validateUserAccess(proceedingJoinPoint, annotation));

    }

    @Test
    public void testShouldNotInvokeFilterWhenSufficientLevel() throws Throwable {

        // GIVEN
        String type = "ANY";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        ResponseEntity<TestResponseObjectNoFilter> testResponse = ResponseEntity.ok(new TestResponseObjectNoFilter());

        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getMaxAccessLevel(type)).thenReturn(AccessLevel.OWNER);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(proceedingJoinPoint.proceed()).thenReturn(testResponse);

        // WHEN
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        // THEN
        verify(testAuthFilter, times(0)).applyFilter(any(), any(), any());
    }

    @Test
    public void testShouldInvokeFilterWhenPermittedLowerLevel() throws Throwable {

        // GIVEN
        String type = "ANY";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        ResponseEntity<TestResponseObject> testResponse = ResponseEntity.ok(new TestResponseObject());

        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getMaxAccessLevel(type)).thenReturn(AccessLevel.RESTRICTED_OWNER);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(annotation.permittedLowerLevels()).thenReturn(new AccessLevel[] { AccessLevel.RESTRICTED_OWNER });
        when(proceedingJoinPoint.proceed()).thenReturn(testResponse);
        when(testAuthFilter.applyFilter(any(), any(), any())).thenReturn(testResponse);

        // WHEN
        Object result = aspect.validateUserAccess(proceedingJoinPoint, annotation);

        // THEN
        verify(testAuthFilter, times(1)).applyFilter(any(), any(), any());

        assertThat(result).isInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;
        assertThat(resultResponseEntity.getBody()).isInstanceOf(TestResponseObject.class);

    }

    @Test
    public void testShouldInvokeFilterWhenPermittedLowerLevelAndArrayListIsResponseBody() throws Throwable {
        // GIVEN
        String type = "ANY";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        ResponseEntity<List<TestResponseObject>> testResponse = ResponseEntity.ok(List.of(new TestResponseObject()));

        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getMaxAccessLevel(type)).thenReturn(AccessLevel.RESTRICTED_OWNER);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(annotation.permittedLowerLevels()).thenReturn(new AccessLevel[] { AccessLevel.RESTRICTED_OWNER });
        when(proceedingJoinPoint.proceed()).thenReturn(testResponse);
        when(testAuthFilter.applyFilter(any(), any(), any())).thenReturn(testResponse);

        // WHEN
        Object result = aspect.validateUserAccess(proceedingJoinPoint, annotation);

        // THEN
        verify(testAuthFilter, times(1)).applyFilter(any(), any(), any());

        assertThat(result).isInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;
        assertThat(resultResponseEntity.getBody()).isInstanceOf(Collection.class);
        assertThat(Collection.class.isAssignableFrom(resultResponseEntity.getBody().getClass())).isTrue();

        Object[] responseAsArray = ((Collection<?>) resultResponseEntity.getBody()).toArray();
        assertThat(responseAsArray.length).isEqualTo(1);
        assertThat(responseAsArray[0]).isInstanceOf(TestResponseObject.class);
    }

    @Test
    public void testShouldInvokeFilterWhenPermittedLowerLevelAndSetIsResponseBody() throws Throwable {
        // GIVEN
        String type = "ANY";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        ResponseEntity<Set<TestResponseObject>> testResponse = ResponseEntity.ok(Set.of(new TestResponseObject()));

        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getMaxAccessLevel(type)).thenReturn(AccessLevel.RESTRICTED_OWNER);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(annotation.permittedLowerLevels()).thenReturn(new AccessLevel[] { AccessLevel.RESTRICTED_OWNER });
        when(proceedingJoinPoint.proceed()).thenReturn(testResponse);
        when(testAuthFilter.applyFilter(any(), any(), any())).thenReturn(testResponse);

        // WHEN
        Object result = aspect.validateUserAccess(proceedingJoinPoint, annotation);

        // THEN
        verify(testAuthFilter, times(1)).applyFilter(any(), any(), any());

        assertThat(result).isInstanceOf(ResponseEntity.class);

        ResponseEntity<?> resultResponseEntity = (ResponseEntity<?>) result;
        assertThat(resultResponseEntity.getBody()).isInstanceOf(Collection.class);
        assertThat(Collection.class.isAssignableFrom(resultResponseEntity.getBody().getClass())).isTrue();

        Object[] responseAsArray = ((Collection<?>) resultResponseEntity.getBody()).toArray();
        assertThat(responseAsArray.length).isEqualTo(1);
        assertThat(responseAsArray[0]).isInstanceOf(TestResponseObject.class);
    }

    @Test
    public void testShouldNotFilterWhenPermittedLowerLevelAndEmptyCollectionIsResponseBody() throws Throwable {
        // GIVEN
        String type = "ANY";
        Object[] args = new Object[1];
        args[0] = caseUUID;

        ResponseEntity<Set<TestResponseObject>> testResponse = ResponseEntity.ok(Set.of());

        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(
            new CaseDataType(type, "al"));
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        when(userService.getMaxAccessLevel(type)).thenReturn(AccessLevel.RESTRICTED_OWNER);
        when(annotation.accessLevel()).thenReturn(AccessLevel.OWNER);
        when(annotation.permittedLowerLevels()).thenReturn(new AccessLevel[] { AccessLevel.RESTRICTED_OWNER });
        when(proceedingJoinPoint.proceed()).thenReturn(testResponse);

        // WHEN
        aspect.validateUserAccess(proceedingJoinPoint, annotation);

        // THEN
        verify(testAuthFilter, times(0)).applyFilter(any(), any(), any());
    }

    // HELPER CLASSES
    private static class TestResponseObject {}

    private static class TestResponseObjectNoFilter {}

    private static class TestAuthFilter implements AuthFilter {

        @Override
        public String getKey() {
            return TestResponseObject.class.getSimpleName();
        }

        @Override
        public Object applyFilter(ResponseEntity<?> responseEntityToFilter,
                                  AccessLevel userAccessLevel,
                                  Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {
            return responseEntityToFilter;
        }

    }

}
