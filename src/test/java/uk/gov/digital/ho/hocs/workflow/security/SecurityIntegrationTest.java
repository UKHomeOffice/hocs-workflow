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
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.PermissionDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;

import java.time.LocalDate;
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
        when(caseworkClient.getStageUser(caseUUID, stageUUID)).thenReturn(userId);
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamUUID);
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(new CaseDataType("MIN", "al"));
        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnForbiddenWhenUserIsNotAllocated() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(teamUUID);
        when(caseworkClient.getStageUser(caseUUID, stageUUID)).thenReturn(UUID.randomUUID());
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(new CaseDataType("MIN", "al"));
        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void shouldReturnUnauthorisedWhenTeamIsNotAllocated() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenReturn(UUID.randomUUID());
        when(infoClient.getCaseTypeByShortCode(caseUUID.toString().substring(34))).thenReturn(new CaseDataType("MIN", "al"));
        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    public void shouldReturnNotFoundIfCaseUUIDNotFound() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        when(caseworkClient.getStageTeam(caseUUID, stageUUID)).thenThrow(new ApplicationExceptions.EntityNotFoundException("Stage not found",LogEvent.CASE_NOT_FOUND));
        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case/" + caseUUID + "/stage/" + stageUUID, HttpMethod.GET, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldCreateCaseWhenUserIsInGroup() {
        UUID caseUUID = UUID.randomUUID();
        when(workflowService.createCase("MIN", LocalDate.now(),new ArrayList<>())).thenReturn(new CreateCaseResponse(caseUUID,"CASE_REF"));
        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<CreateCaseRequest> httpEntity = new HttpEntity<>(new CreateCaseRequest("MIN",LocalDate.now(), new ArrayList<>()), headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case", HttpMethod.POST, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnUnauthorisedForCreateCaseWhenUserIsNotInGroup() {
        headers.add(RequestData.USER_ID_HEADER, userId.toString());
        headers.add(RequestData.GROUP_HEADER, "/RERERCIiIiIiIiIiIiIiIg");
        HttpEntity<CreateCaseRequest> httpEntity = new HttpEntity<>(new CreateCaseRequest("TRO",LocalDate.now(), new ArrayList<>()), headers);
        ResponseEntity<String> result = restTemplate.exchange( getBasePath()  + "/case", HttpMethod.POST, httpEntity, String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private Set<TeamDto>  setupMockTeams(String caseType, int permission) {
        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissions = new HashSet<>();
        permissions.add(new PermissionDto(caseType, AccessLevel.from(permission)));
        TeamDto teamDto = new TeamDto("TEAM 1", teamUUID, true, permissions);
        teamDtos.add(teamDto);
        return teamDtos;
    }

    private String getBasePath() {
        return "http://localhost:"+ port;
    }

}
