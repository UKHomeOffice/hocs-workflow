package uk.gov.digital.ho.hocs.workflow.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.PermissionDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;

@RunWith(MockitoJUnitRunner.class)
public class UserPermissionsServiceTest {

    @Mock
    private RequestData requestData;

    @Mock
    InfoClient infoClient;

    private UserPermissionsService service;

    private final UUID team1Uuid = UUID.fromString("1325fe16-b864-42c7-85c2-7cab2863fe01");
    private final UUID team2Uuid = UUID.fromString("f1825c7d-baff-4c09-8056-2166760ccbd2");
    private final UUID team3Uuid = UUID.fromString("1c1e2f17-d5d9-4ff6-a023-6c40d76e1e9d");

    private final String team1Base64 = Base64UUID.UUIDToBase64String(team1Uuid);
    private final String team2Base64 = Base64UUID.UUIDToBase64String(team2Uuid);
    private final String team3Base64 = Base64UUID.UUIDToBase64String(team3Uuid);

    @Before
    public void setup() {

        String[] GROUPS = ("/" + team2Base64 + "," + "/" + team3Base64 + "," + "/" + team1Base64).split(",");
        when(requestData.groupsArray()).thenReturn(GROUPS);

        Set<TeamDto> teamDtos = new HashSet<>();
        Set<PermissionDto> permissions1 = new HashSet<>();
        permissions1.add(new PermissionDto("MIN", AccessLevel.READ));
        permissions1.add(new PermissionDto("MIN", AccessLevel.OWNER));
        TeamDto teamDto1 = new TeamDto("TEAM 1", team1Uuid, true, permissions1);
        teamDtos.add(teamDto1);

        Set<PermissionDto> permissions2 = new HashSet<>();
        permissions2.add(new PermissionDto("MIN", AccessLevel.READ));
        permissions2.add(new PermissionDto("TRO",   AccessLevel.OWNER));
        TeamDto teamDto2 = new TeamDto("TEAM 2", team2Uuid, true, permissions2);
        teamDtos.add(teamDto2);

        TeamDto teamDto3 = new TeamDto("TEAM 3", team3Uuid, true, new HashSet<>());
        teamDtos.add(teamDto3);

        when(infoClient.getTeams()).thenReturn(teamDtos);

        service = new UserPermissionsService(requestData, infoClient);
    }

    @Test
    public void shouldParseValidUserGroups() {

        assertThat(service.getUserPermission().size()).isEqualTo(3);
    }

    @Test
    public void shouldIgnoreInvalidUserGroups() {

        String[] groups = ("/" + team2Base64 + "," + "INVALID_UUID," + "/" + team3Base64 + ",").split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        assertThat(service.getUserPermission().size()).isEqualTo(2);

    }

    @Test
    public void shouldGetTeamsForUser() {
        String[] groups = ("/" + team3Base64 + "," + "/" + team1Base64).split(",");

        when(requestData.groupsArray()).thenReturn(groups);
        Set<UUID> teams = service.getUserTeams();
        assertThat(teams).size().isEqualTo(2);
        assertThat(teams).contains(team3Uuid);
        assertThat(teams).doesNotContain(team2Uuid);
        assertThat(teams).contains(team1Uuid);
    }

    @Test
    public void shouldGetCaseTypesForUser() {

        Set<String> caseTypes = service.getUserCaseTypes();
        assertThat(caseTypes.stream().anyMatch(c -> c.equals("TRO"))).isTrue();
        assertThat(caseTypes.stream().anyMatch(c -> c.equals("MIN"))).isTrue();
    }

    @Test
    public void shouldGetCaseTypesFromTeamIfTeamIsCaseTypeAdmin() {

        String caseType1 = "CASE_TYPE_1";
        String caseType2 = "CASE_TYPE_2";

        Set<PermissionDto> permissions1 = Set.of(
                new PermissionDto(caseType1, AccessLevel.CASE_ADMIN),
                new PermissionDto(caseType2, AccessLevel.OWNER)
        );

        Set<TeamDto> teams = Set.of(
                new TeamDto(
                        "TEAM 1",
                        Base64UUID.Base64StringToUUID(team1Base64),
                        true,
                        permissions1
                )
        );

        when(infoClient.getTeams()).thenReturn(teams);

        Set<String> caseTypes = service.getCaseTypesIfUserTeamIsCaseTypeAdmin();
        assertThat(caseTypes.size()).isEqualTo(1);
        assertThat(caseTypes.contains(caseType1)).isTrue();
    }

    @Test
    public void isUserOnTeamShouldReturnTrueForTeamsTheyAreAMemberOf() {
        assertThat( service.isUserOnTeam(team2Uuid)).isTrue();
    }

    @Test
    public void isUserOnTeamShouldReturnFalseForTeamsTheyAreNotAMemberOf() {
        assertThat(service.isUserOnTeam(UUID.randomUUID())).isFalse();
    }
}
