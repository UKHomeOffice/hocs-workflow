package uk.gov.digital.ho.hocs.workflow.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.PermissionDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionsServiceTest {

    @Mock
    private RequestData requestData;

    @Mock
    InfoClient infoClient;

    private UserPermissionsService service;

    private String uuid1 = Base64UUID.UUIDToBase64String(UUID.fromString("1325fe16-b864-42c7-85c2-7cab2863fe01"));
    private String uuid2 = Base64UUID.UUIDToBase64String(UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2"));
    private String uuid3 = Base64UUID.UUIDToBase64String(UUID.fromString("1c1e2f17-d5d9-4ff6-a023-6c40d76e1e9d"));


    @Before
    public void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        setupInfoClientMocks();
    }


    @Test
    public void shouldParseValidUserGroups() {

        String[] groups =
                ("/" + uuid2 + "," +
                        "/" + uuid3 + "," +
                        "/" +  uuid1).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);
        assertThat(service.getUserPermission().size()).isEqualTo(3);
    }


    @Test
    public void shouldIgnoreInvalidUserGroups() {

        String[] groups =
                ("/" + uuid2 + "," +
                        "INVALID_UUID," +
                        "/" + uuid3 + ","
                       ).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);
        assertThat(service.getUserPermission().size()).isEqualTo(2);

    }


    @Test
    public void shouldGetTeamsForUser() {
        String[] groups =
                ("/" + uuid3 + "," +
                        "/" + uuid1).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);
        Set<UUID> teams = service.getUserTeams();
        assertThat(teams).size().isEqualTo(2);
        assertThat(teams).contains(UUID.fromString("1c1e2f17-d5d9-4ff6-a023-6c40d76e1e9d"));
        assertThat(teams).doesNotContain(UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2"));
        assertThat(teams).contains(UUID.fromString("1325fe16-b864-42c7-85c2-7cab2863fe01"));
    }

    @Test
    public void shouldGetCaseTypesForUser() {
        String[] groups =
                ("/" + uuid2 + "," +
                        "/" + uuid3 + "," +
                        "/" + uuid1).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        service = new UserPermissionsService(requestData, infoClient);
        Set<String> caseTypes = service.getUserCaseTypes();
        assertThat(caseTypes.stream().anyMatch(c -> c.equals("TRO"))).isTrue();
        assertThat(caseTypes.stream().anyMatch(c -> c.equals("MIN"))).isTrue();
    }

    private void setupInfoClientMocks() {

        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissions1 = new HashSet<>();
        permissions1.add(new PermissionDto("MIN", AccessLevel.READ));
        permissions1.add(new PermissionDto("MIN", AccessLevel.OWNER));
        TeamDto teamDto1 = new TeamDto("TEAM 1", UUID.fromString("1325fe16-b864-42c7-85c2-7cab2863fe01"), true, permissions1);
        teamDtos.add(teamDto1);

        Set<PermissionDto> permissions2 = new HashSet<>();
        permissions2.add(new PermissionDto("MIN", AccessLevel.READ));
        permissions2.add(new PermissionDto("TRO",   AccessLevel.OWNER));
        TeamDto teamDto2 = new TeamDto("TEAM 2", UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2"), true, permissions2);
        teamDtos.add(teamDto2);

        TeamDto teamDto3 = new TeamDto("TEAM 3", UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2"), true, new HashSet<>());
        teamDtos.add(teamDto3);

        when(infoClient.getTeams()).thenReturn(teamDtos);
    }
}