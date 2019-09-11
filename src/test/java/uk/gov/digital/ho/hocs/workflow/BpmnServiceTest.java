package uk.gov.digital.ho.hocs.workflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BpmnServiceTest {

    @Mock
    private CaseworkClient caseworkClient;

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private InfoClient infoClient;

    private BpmnService bpmnService;

    private String caseUUID = UUID.randomUUID().toString();

    private String stageUUID = UUID.randomUUID().toString();


    @Before
    public void setup() {
        bpmnService = new BpmnService(caseworkClient, camundaClient, infoClient);
    }

    @Test
    public void shouldNotUpdateDataNoNewTeams() {
        bpmnService.updateTeamSelection(caseUUID, stageUUID, null, null);

        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldUpdateTeamForRegionAndStage(){
        UUID regionUUID = UUID.randomUUID();
        TeamDto teamDto = mock(TeamDto.class);
        when(teamDto.getUuid()).thenReturn(UUID.fromString("387ceb7a-ac90-417c-a510-a22260981ca7"));
        when(teamDto.getDisplayName()).thenReturn("teamDisplayName");
        when(infoClient.getTeamForRegionAndStage(UUID.fromString(caseUUID),regionUUID,"stageType")).thenReturn(teamDto);

        bpmnService.updateTeamForRegionAndStage(caseUUID, stageUUID, regionUUID.toString(), "stageType", "teamKeyName", "teamUUIDKey");

        verify(infoClient, times(1)).getTeamForRegionAndStage(UUID.fromString(caseUUID),regionUUID,"stageType");
        verify(camundaClient, times(1)).updateTask(eq(UUID.fromString(stageUUID)), any());
        verify(caseworkClient, times(1)).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());

        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
    }

    @Test
    public void shouldUpdateDataNewDraftingTeams() {

        UUID draftingString = UUID.randomUUID();

        when(infoClient.getTeam(draftingString)).thenReturn(new TeamDto("Team1", draftingString, true, new HashSet<>()));
        doNothing().when(camundaClient).updateTask(eq(UUID.fromString(stageUUID)),any());
        doNothing().when(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)),eq(UUID.fromString(stageUUID)),any());

        bpmnService.updateTeamSelection(caseUUID, stageUUID, draftingString.toString(), null);

        verify(infoClient, times(1)).getTeam(draftingString);
        verify(camundaClient, times(1)).updateTask(eq(UUID.fromString(stageUUID)), any());
        verify(caseworkClient, times(1)).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());

        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldUpdateDataNewPOTeams() {

        UUID privateOfficeString = UUID.randomUUID();

        when(infoClient.getTeam(privateOfficeString)).thenReturn(new TeamDto("Team1", privateOfficeString, true, new HashSet<>()));
        doNothing().when(camundaClient).updateTask(eq(UUID.fromString(stageUUID)),any());
        doNothing().when(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)),eq(UUID.fromString(stageUUID)),any());

        bpmnService.updateTeamSelection(caseUUID, stageUUID, null, privateOfficeString.toString());

        verify(infoClient, times(1)).getTeam(privateOfficeString);
        verify(camundaClient, times(1)).updateTask(eq(UUID.fromString(stageUUID)), any());
        verify(caseworkClient, times(1)).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());

        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldUpdateDataNewTeams() {

        UUID draftingString = UUID.randomUUID();
        UUID privateOfficeString = UUID.randomUUID();

        when(infoClient.getTeam(privateOfficeString)).thenReturn(new TeamDto("Team1", privateOfficeString, true, new HashSet<>()));
        when(infoClient.getTeam(draftingString)).thenReturn(new TeamDto("Team1", privateOfficeString, true,  new HashSet<>()));
        doNothing().when(camundaClient).updateTask(eq(UUID.fromString(stageUUID)),any());
        doNothing().when(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)),eq(UUID.fromString(stageUUID)),any());

        bpmnService.updateTeamSelection(caseUUID, stageUUID, draftingString.toString(), privateOfficeString.toString());

        verify(infoClient, times(1)).getTeam(draftingString);
        verify(infoClient, times(1)).getTeam(privateOfficeString);
        verify(camundaClient, times(1)).updateTask(eq(UUID.fromString(stageUUID)), any());
        verify(caseworkClient, times(1)).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());

        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldUpdateTeamSelectionByTeamId(){
        UUID teamUUID = UUID.randomUUID();
        String teamNameProperty= "NewTestStageTeamName";
        String teamIdProperty = "NewTestStageTeamId";
        String teamName = "Team1";

        Map<String, String> expectedData = new HashMap<>();
        expectedData.put(teamIdProperty, teamUUID.toString());
        expectedData.put(teamNameProperty, teamName);

        when(infoClient.getTeam(teamUUID)).thenReturn(new TeamDto(teamName, teamUUID, true, new HashSet<>()));
        doNothing().when(camundaClient).updateTask(eq(UUID.fromString(stageUUID)),eq(expectedData));
        doNothing().when(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)),eq(UUID.fromString(stageUUID)),eq(expectedData));

        bpmnService.updateTeamSelectionByTeamId(caseUUID, stageUUID, teamUUID.toString(), teamNameProperty, teamIdProperty);

        verify(infoClient, times(1)).getTeam(teamUUID);
        verify(camundaClient, times(1)).updateTask(eq(UUID.fromString(stageUUID)), any());
        verify(caseworkClient, times(1)).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());

        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }
}