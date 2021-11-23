package uk.gov.digital.ho.hocs.workflow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.workflow.api.WorkflowService;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.workflow.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.workflow.application.LogEvent;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCorrespondentResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.PermissionDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Profile("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SecurityIntegrationTest {

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders headers = new HttpHeaders();

    @LocalServerPort
    int port;

    @MockBean
    CaseworkClient caseworkClient;

    @MockBean
    InfoClient infoClient;

    @MockBean
    WorkflowService workflowService;

    @MockBean
    UserPermissionsService userPermissionsService;

    UUID userId = UUID.randomUUID();
    UUID teamUUID = UUID.fromString("44444444-2222-2222-2222-222222222222");

    @Autowired
    ObjectMapper mapper;

    @Before
    public void setup() {
        when(infoClient.getTeams()).thenReturn(setupMockTeams("MIN", 5));
    }


    @Test
    public void shouldGetStageDataWhenUserAndTeamAreAllocated() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();

        setUpAdminTeamLogic(caseUUID, false);
        when(userPermissionsService.getUserTeams()).thenReturn(Set.of(teamUUID));
        when(caseworkClient.getStageUser(caseUUID, stageUUID)).thenReturn(userId);
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamUUID);
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(new CaseDataType("MIN", "al"));

        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, new HttpEntity<>(null), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnForbiddenWhenUserIsNotAllocated() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();

        setUpAdminTeamLogic(caseUUID, false);
        when(userPermissionsService.getUserTeams()).thenReturn(Set.of(teamUUID));
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamUUID);
        when(caseworkClient.getStageUser(caseUUID, stageUUID)).thenReturn(UUID.randomUUID());
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(new CaseDataType("MIN", "al"));

        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, new HttpEntity<>(null), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldReturnUnauthorisedWhenTeamIsNotAllocated() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        setUpAdminTeamLogic(caseUUID, false);
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(UUID.randomUUID());
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(new CaseDataType("MIN", "al"));

        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, new HttpEntity<>(null), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnNotFoundIfCaseUUIDNotFound() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        setUpAdminTeamLogic(caseUUID, false);
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenThrow(new ApplicationExceptions.EntityNotFoundException("Stage not found",LogEvent.CASE_NOT_FOUND));
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, new HttpEntity<>(null), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldCreateCaseWhenUserIsInGroup() {
        UUID caseUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        String caseType = "MIN";
        Map<String, String> requestData = new HashMap<>();

        setUpAdminTeamLogic(caseUUID, false);
        when(userPermissionsService.getUserTeams()).thenReturn(Set.of(teamUUID));
        when(userPermissionsService.getMaxAccessLevel(caseType)).thenReturn(AccessLevel.from(5));
        when(workflowService.createCase(caseType, LocalDate.now(),new ArrayList<>(), userUUID, null, requestData)).thenReturn(new CreateCaseResponse(caseUUID,"CASE_REF"));

        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        HttpEntity<CreateCaseRequest> httpEntity = new HttpEntity<>(new CreateCaseRequest("MIN",LocalDate.now(), requestData, new ArrayList<>(), null), headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case", HttpMethod.POST, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnUnauthorisedForCreateCaseWhenUserIsNotInGroup() {
        String caseType = "TRO";
        when(userPermissionsService.getMaxAccessLevel(caseType)).thenReturn(AccessLevel.from(0));
        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        HttpEntity<CreateCaseRequest> httpEntity = new HttpEntity<>(new CreateCaseRequest(caseType,LocalDate.now(), new HashMap<>(), new ArrayList<>(), null), headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case", HttpMethod.POST, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldCreateCaseWhenUserInAdminTeam() {
        String caseType = "MIN";

        when(userPermissionsService.getMaxAccessLevel(caseType)).thenReturn(AccessLevel.from(6));

        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        HttpEntity<CreateCaseRequest> httpEntity = new HttpEntity<>(new CreateCaseRequest("MIN",LocalDate.now(), new HashMap<>(), new ArrayList<>(), null), headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case", HttpMethod.POST, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private Set<TeamDto> setupMockTeams(String caseType, int permission) {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissions = new HashSet<>();
        permissions.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", teamUUID, true, permissions);
        teamDtos.add(teamDto);
        return teamDtos;
    }

    private void setUpAdminTeamLogic(UUID caseUUID, boolean isAdminTeam) {
        LocalDate localDate = LocalDate.now();

        String caseType = "SOME_CASE_TYPE";
        String teamCaseType = isAdminTeam ? "SOME_CASE_TYPE" : "SOME_OTHER_CASE_TYPE";
        Set<String> adminTeamCaseTypes = Set.of(teamCaseType);

        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, ZonedDateTime.now(), caseType, "REF", new HashMap<>(), localDate, localDate,
                UUID.randomUUID(), new GetTopicResponse(), UUID.randomUUID(), new GetCorrespondentResponse(), false
        );

        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin()).thenReturn(adminTeamCaseTypes);
        when(userPermissionsService.getUserId()).thenReturn(userId);
    }

    private String getBasePath() {
        return "http://localhost:"+ port;
    }

}
