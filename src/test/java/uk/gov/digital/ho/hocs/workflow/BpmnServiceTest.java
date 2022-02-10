package uk.gov.digital.ho.hocs.workflow;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.api.WorkflowService;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.*;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.UnitDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.util.NoteType;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private Clock clock;

    @Mock
    private WorkflowService workflowService;

    private BpmnService bpmnService;

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final String dateReceived = LocalDate.now().toString();


    @Before
    public void setup() {
        bpmnService = new BpmnService(caseworkClient, camundaClient, infoClient, clock, workflowService);
    }

    @Test
    public void testShouldCalculateTotals() {
        when(caseworkClient.calculateTotals(caseUUID, stageUUID, "list")).thenReturn(null);

        bpmnService.calculateTotals(caseUUID.toString(), stageUUID.toString(), "list");

        verify(caseworkClient).calculateTotals(caseUUID, stageUUID, "list");
        verifyNoMoreInteractions(caseworkClient);
        verifyNoMoreInteractions(camundaClient);
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void testShouldUpdateDeadline() {

        bpmnService.updateDeadline(caseUUID.toString(), stageUUID.toString(), dateReceived);

        verify(caseworkClient).updateDateReceived(caseUUID, stageUUID, LocalDate.parse(dateReceived));
        verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shoudUpdateDispatchDeadlineDate() {

        bpmnService.updateDispatchDeadlineDate(caseUUID.toString(), stageUUID.toString(), dateReceived);

        verify(caseworkClient).updateDispatchDeadlineDate(caseUUID, stageUUID, LocalDate.parse(dateReceived));
        verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shoudUpdateDeadlineDays() {
        doNothing().when(caseworkClient).updateDeadlineDays(caseUUID, stageUUID, 123);

        bpmnService.updateDeadlineDays(caseUUID.toString(), stageUUID.toString(), "123");

        verify(caseworkClient).updateDeadlineDays(caseUUID, stageUUID, 123);
        verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shoudUpdateStageDeadline() {
        doNothing().when(caseworkClient).updateStageDeadline(caseUUID, stageUUID, "TEST", 7);

        bpmnService.updateStageDeadline(caseUUID.toString(), stageUUID.toString(), "TEST", "7");

        verify(caseworkClient).updateStageDeadline(caseUUID, stageUUID, "TEST", 7);
        verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldSaveDraftingTeam() {
        UUID draftingString = UUID.randomUUID();

        String teamName = "Team1";
        String unitName = "Unit1";
        TeamDto team = new TeamDto(teamName, draftingString, true, new HashSet<>());
        UnitDto unit = new UnitDto(unitName, "TEST");

        Map<String, String> teamsForTopic = new HashMap<>();
        teamsForTopic.put("OverrideDraftingTeamUUID", "");
        teamsForTopic.put("OverrideDraftingTeamName", "");
        teamsForTopic.put("OverrideDraftingTeamUnitHistoricName", "");
        teamsForTopic.put("DraftingTeamUUID", draftingString.toString());
        teamsForTopic.put("DraftingTeamName", teamName);
        teamsForTopic.put("DraftingTeamUnitHistoricName", unitName);

        when(infoClient.getTeam(draftingString)).thenReturn(team);
        when(infoClient.getUnitForTeam(draftingString)).thenReturn(unit);

        bpmnService.setDraftingTeam(caseUUID.toString(), stageUUID.toString(), draftingString.toString());

        verify(infoClient, times(1)).getTeam(draftingString);
        verify(infoClient, times(1)).getUnitForTeam(draftingString);
        verify(camundaClient, times(1)).updateTask(stageUUID, teamsForTopic);
        verify(caseworkClient, times(1)).updateCase(caseUUID, stageUUID, teamsForTopic);

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);

    }

    @Test
    public void shouldNotUpdateDataNoNewTeams() {
        bpmnService.updateTeamSelection(caseUUID.toString(), stageUUID.toString(), null, null);

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldNotUpdateDataNoNewTeamsWhenEmptyString() {
        bpmnService.updateTeamSelection(caseUUID.toString(), stageUUID.toString(), "", "");

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldUpdateDataNewDraftingTeams() {

        UUID draftingString = UUID.randomUUID();

        String teamName = "Team1";
        String unitName = "Unit1";

        TeamDto team = new TeamDto(teamName, draftingString, true, new HashSet<>());
        UnitDto unit = new UnitDto(unitName, "T");
        Map<String, String> teamsForTopic = new HashMap<>();
        teamsForTopic.put("OverrideDraftingTeamUUID", draftingString.toString());
        teamsForTopic.put("OverrideDraftingTeamName", teamName);
        teamsForTopic.put("OverrideDraftingTeamUnitHistoricName", unitName);

        when(infoClient.getTeam(draftingString)).thenReturn(team);
        when(infoClient.getUnitForTeam(draftingString)).thenReturn(unit);

        bpmnService.updateTeamSelection(caseUUID.toString(), stageUUID.toString(), draftingString.toString(), null);

        verify(infoClient, times(1)).getTeam(draftingString);
        verify(infoClient, times(1)).getUnitForTeam(draftingString);
        verify(camundaClient, times(1)).updateTask(stageUUID, teamsForTopic);
        verify(caseworkClient, times(1)).updateCase(caseUUID, stageUUID, teamsForTopic);

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldUpdateDataRemoveOverrideDraftingTeams() {

        UUID draftingString = UUID.randomUUID();

        String teamName = "Team1";
        String unitName = "Unit1";
        TeamDto team = new TeamDto(teamName, draftingString, false, new HashSet<>());
        UnitDto unit = new UnitDto(unitName, "TEST");
        Map<String, String> teamsForTopic = new HashMap<>();
        teamsForTopic.put("OverrideDraftingTeamUUID", "");
        teamsForTopic.put("OverrideDraftingTeamName", "");
        teamsForTopic.put("OverrideDraftingTeamUnitHistoricName", "");

        when(infoClient.getTeam(draftingString)).thenReturn(team);
        when(infoClient.getUnitForTeam(draftingString)).thenReturn(unit);

        bpmnService.updateTeamSelection(caseUUID.toString(), stageUUID.toString(), draftingString.toString(), null);

        verify(infoClient, times(1)).getTeam(draftingString);
        verify(infoClient, times(1)).getUnitForTeam(draftingString);
        verify(camundaClient, times(1)).updateTask(stageUUID, teamsForTopic);
        verify(caseworkClient, times(1)).updateCase(caseUUID, stageUUID, teamsForTopic);

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldUpdateDataNewPOTeams() {

        UUID privateOfficeString = UUID.randomUUID();

        String teamName = "Team1";
        String unitName = "Unit1";
        TeamDto team = new TeamDto(teamName, privateOfficeString, true, new HashSet<>());
        UnitDto unit = new UnitDto(unitName, "TEST");
        Map<String, String> teamsForTopic = new HashMap<>();
        teamsForTopic.put("OverridePOTeamUUID", privateOfficeString.toString());
        teamsForTopic.put("OverridePOTeamName", teamName);
        teamsForTopic.put("OverridePOTeamUnitHistoricName", unitName);

        when(infoClient.getTeam(privateOfficeString)).thenReturn(team);
        when(infoClient.getUnitForTeam(privateOfficeString)).thenReturn(unit);

        bpmnService.updateTeamSelection(caseUUID.toString(), stageUUID.toString(), null, privateOfficeString.toString());

        verify(infoClient, times(1)).getTeam(privateOfficeString);
        verify(infoClient, times(1)).getUnitForTeam(privateOfficeString);
        verify(camundaClient, times(1)).updateTask(stageUUID, teamsForTopic);
        verify(caseworkClient, times(1)).updateCase(caseUUID, stageUUID, teamsForTopic);

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldUpdateDataRemoveOverridePOTeams() {

        UUID privateOfficeString = UUID.randomUUID();

        String teamName = "Team1";
        TeamDto team = new TeamDto(teamName, privateOfficeString, false, new HashSet<>());
        Map<String, String> teamsForTopic = new HashMap<>();
        teamsForTopic.put("OverridePOTeamUUID", "");
        teamsForTopic.put("OverridePOTeamName", "");
        teamsForTopic.put("OverridePOTeamUnitHistoricName", "");

        when(infoClient.getTeam(privateOfficeString)).thenReturn(team);

        bpmnService.updateTeamSelection(caseUUID.toString(), stageUUID.toString(), null, privateOfficeString.toString());

        verify(infoClient, times(1)).getTeam(privateOfficeString);
        verify(infoClient, times(1)).getUnitForTeam(privateOfficeString);
        verify(camundaClient, times(1)).updateTask(stageUUID, teamsForTopic);
        verify(caseworkClient, times(1)).updateCase(caseUUID, stageUUID, teamsForTopic);

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldCaseHasPrimaryCorrespondentTypeWhenPrimaryWrongTypeReturnFalse() {
        UUID caseUUID = UUID.randomUUID();
        UUID primaryUUID = UUID.randomUUID();
        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null, null, null, null, null, null, primaryUUID, null, false);
        GetCorrespondentResponse correspondent1 = new GetCorrespondentResponse(
                UUID.randomUUID(), null, "NO", null, null, null, null, null, null);
        GetCorrespondentResponse correspondent2 = new GetCorrespondentResponse(
                primaryUUID, null, "NO-MATCH", null, null, null, null, null, null);
        GetCorrespondentsResponse correspondents = new GetCorrespondentsResponse(
                List.of(correspondent1, correspondent2));
        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(caseworkClient.getCorrespondentsForCase(caseUUID)).thenReturn(correspondents);

        boolean result = bpmnService.caseHasPrimaryCorrespondentType(caseUUID.toString(), "MATCHING");

        verify(caseworkClient).getCase(caseUUID);
        verify(caseworkClient).getCorrespondentsForCase(caseUUID);
         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
        assertThat(result).isFalse();
    }

    @Test
    public void shouldCaseHasPrimaryCorrespondentTypeWhenMatchReturnTrue() {
        UUID caseUUID = UUID.randomUUID();
        UUID primaryUUID = UUID.randomUUID();
        GetCaseworkCaseDataResponse caseData = new GetCaseworkCaseDataResponse(
                caseUUID, null, null, null, null, null, null, null, null, primaryUUID, null, false);
        GetCorrespondentResponse correspondent1 = new GetCorrespondentResponse(
                UUID.randomUUID(), null, "NO", null, null, null, null, null, null);
        GetCorrespondentResponse correspondent2 = new GetCorrespondentResponse(
                primaryUUID, null, "MATCHING", null, null, null, null, null, null);
        GetCorrespondentsResponse correspondents = new GetCorrespondentsResponse(
                List.of(correspondent1, correspondent2));
        when(caseworkClient.getCase(caseUUID)).thenReturn(caseData);
        when(caseworkClient.getCorrespondentsForCase(caseUUID)).thenReturn(correspondents);

        boolean result = bpmnService.caseHasPrimaryCorrespondentType(caseUUID.toString(), "MATCHING");

        verify(caseworkClient).getCase(caseUUID);
        verify(caseworkClient).getCorrespondentsForCase(caseUUID);
         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
        assertThat(result).isTrue();
    }

    @Test
    public void shouldNotAssignInactiveTeam() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID topicUUID = UUID.randomUUID();
        String stageName = "MOCK_STAGE_TYPE";
        String teamName = "Team Name";
        UUID teamUUID = UUID.randomUUID();

        when(infoClient.getTeamForTopicAndStage(caseUUID, topicUUID, stageName)).thenReturn(new TeamDto(teamName, teamUUID, false, new HashSet<>()));
        doNothing().when(camundaClient).updateTask(eq(stageUUID), any());
        doNothing().when(caseworkClient).updateCase(eq(caseUUID), eq(stageUUID), any());

        bpmnService.updateTeamsForPrimaryTopic(caseUUID.toString(), stageUUID.toString(), topicUUID.toString(), stageName, teamName, teamUUID.toString(), "unitNameKey");

        verify(camundaClient, times(1)).updateTask(eq(stageUUID), eq(new HashMap<>()));
        verify(caseworkClient, times(1)).updateCase(eq(caseUUID), eq(stageUUID), any());

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
    }

    @Test
    public void shouldAssignActiveTeam() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID topicUUID = UUID.randomUUID();
        String stageName = "MOCK_STAGE_TYPE";
        String teamName = "Team Name";
        String unitName = "Unit Name";
        UUID teamUUID = UUID.randomUUID();

        when(infoClient.getTeamForTopicAndStage(caseUUID, topicUUID, stageName)).thenReturn(new TeamDto(teamName, teamUUID, true, new HashSet<>()));
        when(infoClient.getUnitForTeam(teamUUID)).thenReturn(new UnitDto(unitName, "TEST"));
        doNothing().when(camundaClient).updateTask(eq(stageUUID), any());
        doNothing().when(caseworkClient).updateCase(eq(caseUUID), eq(stageUUID), any());

        bpmnService.updateTeamsForPrimaryTopic(caseUUID.toString(), stageUUID.toString(), topicUUID.toString(), stageName, teamName, teamUUID.toString(), "unitNameKey");

        verify(camundaClient, times(1)).updateTask(eq(stageUUID), valueCapture.capture());
        verify(caseworkClient, times(1)).updateCase(eq(caseUUID), eq(stageUUID), any());

        assertThat(valueCapture.getValue().size()).isEqualTo(3);

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
    }

    @Test
    public void shouldUpdateDataNewTeams() {

        UUID draftingString = UUID.randomUUID();
        UUID privateOfficeString = UUID.randomUUID();

        when(infoClient.getTeam(privateOfficeString)).thenReturn(new TeamDto("Team1", privateOfficeString, true, new HashSet<>()));
        when(infoClient.getUnitForTeam(privateOfficeString)).thenReturn(new UnitDto("Unit1", "Unit1"));
        when(infoClient.getTeam(draftingString)).thenReturn(new TeamDto("Team1", privateOfficeString, true, new HashSet<>()));
        when(infoClient.getUnitForTeam(draftingString)).thenReturn(new UnitDto("Unit1", "Unit1"));
        doNothing().when(camundaClient).updateTask(eq(stageUUID), any());
        doNothing().when(caseworkClient).updateCase(eq(caseUUID), eq(stageUUID), any());

        bpmnService.updateTeamSelection(caseUUID.toString(), stageUUID.toString(), draftingString.toString(), privateOfficeString.toString());

        verify(infoClient, times(1)).getTeam(draftingString);
        verify(infoClient, times(1)).getUnitForTeam(draftingString);
        verify(infoClient, times(1)).getTeam(privateOfficeString);
        verify(infoClient, times(1)).getUnitForTeam(privateOfficeString);
        verify(camundaClient, times(1)).updateTask(eq(stageUUID), any());
        verify(caseworkClient, times(1)).updateCase(eq(caseUUID), eq(stageUUID), any());

         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldUpdateTeamByStageAndTexts() {

        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        Map<String, String> teamForText = new HashMap<>();
        teamForText.put("key", "value");
        when(caseworkClient.updateTeamByStageAndTexts(
                eq(caseUUID), eq(stageUUID), eq("stageType"), eq("teamUUIDKey"), eq("teamNameKey"), any()))
                .thenReturn(teamForText);
        doNothing().when(camundaClient).updateTask(eq(stageUUID), any());
        doNothing().when(caseworkClient).updateCase(eq(caseUUID), eq(stageUUID), any());

        bpmnService.updateTeamByStageAndTexts(
                caseUUID.toString(), stageUUID.toString(), "stageType", "teamUUIDKey", "teamNameKey",
                "Text1", "Text2", "Text3");

        verify(caseworkClient).updateTeamByStageAndTexts(
                eq(caseUUID), eq(stageUUID), eq("stageType"), eq("teamUUIDKey"), eq("teamNameKey"), any());
        verify(camundaClient, times(1)).updateTask(eq(stageUUID), any());
        verify(caseworkClient, times(1)).updateCase(eq(caseUUID), eq(stageUUID), any());
         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldUpdateValue() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        bpmnService.updateValue(caseUUID.toString(), stageUUID.toString(), "testKey", "testValue");

        verify(camundaClient, times(1)).updateTask(eq(stageUUID), valueCapture.capture());
        verify(caseworkClient, times(1)).updateCase(eq(caseUUID), eq(stageUUID), any());
        assertThat(valueCapture.getValue().size()).isEqualTo(1);
        assertThat(valueCapture.getValue().keySet()).contains("testKey");
        assertThat(valueCapture.getValue().values()).contains("testValue");
        verifyNoMoreInteractions(caseworkClient);
        verifyNoMoreInteractions(camundaClient);
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldUpdateValue_multiple() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);
        Map<String, String> expectedData = Map.of("key1", "value1", "key2", "value3", "key3", "value3");
        bpmnService.updateValue(caseUUID.toString(), stageUUID.toString(), "key1", "value1", "key2", "value3", "key3", "value3");

        verify(camundaClient).updateTask(eq(stageUUID), valueCapture.capture());
        verify(caseworkClient).updateCase(eq(caseUUID), eq(stageUUID), eq(expectedData));
        assertThat(valueCapture.getValue().size()).isEqualTo(3);
        assertThat(valueCapture.getValue()).isEqualTo(expectedData);
        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient);
    }

    @Test(expected = ApplicationExceptions.InvalidMethodArgumentException.class)
    public void shouldFailToUpdateValue() {
        bpmnService.updateCaseValue(caseUUID.toString(), stageUUID.toString(), "key1", "value1", "key2", "value3", "key3", "value3", "key4");
    }

    @Test
    public void shouldUpdateCaseValue() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        bpmnService.updateCaseValue(caseUUID.toString(), stageUUID.toString(), "testKey", "testValue");

        verify(caseworkClient, times(1)).updateCase(eq(caseUUID), eq(stageUUID), valueCapture.capture());
        assertThat(valueCapture.getValue().size()).isEqualTo(1);
        assertThat(valueCapture.getValue().keySet()).contains("testKey");
        assertThat(valueCapture.getValue().values()).contains("testValue");
        verifyNoMoreInteractions(caseworkClient);
        verifyNoMoreInteractions(camundaClient);
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldUpdateCaseValue_multiple() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        Map<String, String> expectedData = Map.of("key1", "value1", "key2", "value3", "key3", "value3");
        bpmnService.updateCaseValue(caseUUID.toString(), stageUUID.toString(), "key1", "value1", "key2", "value3", "key3", "value3");

        verify(caseworkClient).updateCase(eq(caseUUID), eq(stageUUID), valueCapture.capture());
        assertThat(valueCapture.getValue().size()).isEqualTo(3);
        assertThat(valueCapture.getValue()).isEqualTo(expectedData);
        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient);
    }

    @Test(expected = ApplicationExceptions.InvalidMethodArgumentException.class)
    public void shouldFailToUpdateCaseValue() {

        bpmnService.updateCaseValue(caseUUID.toString(), stageUUID.toString(), "key1", "value1", "key2", "value3", "key3", "value3", "key4");

    }

    @Test
    public void shouldBlankCaseValues() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        bpmnService.blankCaseValues(caseUUID.toString(), stageUUID.toString(), "key1", "key2", "key3");

        verify(caseworkClient).updateCase(eq(caseUUID), eq(stageUUID), valueCapture.capture());
        assertThat(valueCapture.getValue().size()).isEqualTo(3);
        assertThat(valueCapture.getValue().keySet()).containsOnly("key1", "key2", "key3");
        assertThat(valueCapture.getValue().values()).containsOnly("");
        verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient);
    }

    @Test
    public void shouldCreateNewStage_whenUserIsInTeam() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        UUID allocationTeam = UUID.randomUUID();
        UUID allocatedUserId = UUID.randomUUID();

        UUID expectedStageUUID = UUID.randomUUID();
        when(caseworkClient.createStage(eq(caseUUID), any(CreateCaseworkStageRequest.class))).thenReturn(expectedStageUUID);

        String resultUUID = bpmnService.createStage(caseUUID.toString(), null, stageType, allocationType, allocationTeam.toString(), allocatedUserId.toString());

        assertThat(resultUUID).isEqualTo(expectedStageUUID.toString());
        verify(caseworkClient).createStage(eq(caseUUID), any(CreateCaseworkStageRequest.class));
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldCreateNewStage_whenUserNotInTeam() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        UUID allocationTeam = UUID.randomUUID();
        UUID allocatedUserId = UUID.randomUUID();
        UUID expectedStageUUID = UUID.randomUUID();

        when(caseworkClient.createStage(eq(caseUUID), any(CreateCaseworkStageRequest.class))).thenReturn(expectedStageUUID);

        String resultUUID = bpmnService.createStage(caseUUID.toString(), null, stageType, allocationType, allocationTeam.toString(), allocatedUserId.toString());

        assertThat(resultUUID).isEqualTo(expectedStageUUID.toString());
        verify(caseworkClient).createStage(eq(caseUUID), any(CreateCaseworkStageRequest.class));
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldCreateNewStage_NoUserParam() {

        // GIVEN
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        UUID allocationTeam = UUID.randomUUID();
        UUID expectedStageUUID = UUID.randomUUID();

        when(caseworkClient.createStage(eq(caseUUID), any(CreateCaseworkStageRequest.class))).thenReturn(expectedStageUUID);

        // WHEN
        String resultUUID = bpmnService.createStage(caseUUID.toString(), null, stageType, allocationType, allocationTeam.toString());

        // THEN
        assertThat(resultUUID).isEqualTo(expectedStageUUID.toString());
        verify(caseworkClient).createStage(eq(caseUUID), any(CreateCaseworkStageRequest.class));
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);

    }

    @Test
    public void shouldCreateNewStage_NoAllocationTeam() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        UUID allocatedUserId = UUID.randomUUID();
        UUID expectedStageUUID = UUID.randomUUID();
        UUID expectedAllocationTeam = UUID.randomUUID();

        when(caseworkClient.createStage(eq(caseUUID), any(CreateCaseworkStageRequest.class))).thenReturn(expectedStageUUID);

        String resultUUID = bpmnService.createStage(caseUUID.toString(), null, stageType, allocationType, null, allocatedUserId.toString());

        assertThat(resultUUID).isEqualTo(expectedStageUUID.toString());
        verify(caseworkClient).createStage(eq(caseUUID), any(CreateCaseworkStageRequest.class));
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldRecreateStage() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        UUID allocationTeam = UUID.randomUUID();
        UUID allocatedUserId = UUID.randomUUID();


        String resultUUID = bpmnService.createStage(caseUUID.toString(), stageUUID.toString(), stageType, allocationType, allocationTeam.toString(), allocatedUserId.toString());

        assertThat(resultUUID).isEqualTo(stageUUID.toString());
        verify(caseworkClient).recreateStage(eq(caseUUID), any(RecreateCaseworkStageRequest.class));
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldRecreateStage_NoUserUUIDProvided() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        UUID allocationTeam = UUID.randomUUID();

        String resultUUID = bpmnService.createStage(caseUUID.toString(), stageUUID.toString(), stageType, allocationType, allocationTeam.toString(), null);

        assertThat(resultUUID).isEqualTo(stageUUID.toString());
        verify(caseworkClient).recreateStage(eq(caseUUID), any(RecreateCaseworkStageRequest.class));
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);

    }

    @Test
    public void updateCount_nullValue() {
        String variableName = "testVariableName";
        int additive = 1;

        when(caseworkClient.getDataValue(caseUUID.toString(), variableName)).thenReturn(null);

        bpmnService.updateCount(caseUUID.toString(), variableName, additive);

        verify(caseworkClient).getDataValue(caseUUID.toString(), variableName);
        verify(caseworkClient).updateDataValue(caseUUID.toString(), variableName, "1");

        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void should_wipeVariables() {
        UUID caseUuid = UUID.randomUUID();
        UUID stageUuid = UUID.randomUUID();

        bpmnService.wipeVariables(caseUuid.toString(), stageUuid.toString(), "key1", "key2", "key3");

        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        verify(caseworkClient).updateCase(eq(caseUuid), eq(stageUuid), valueCapture.capture());
        verify(camundaClient).removeTaskVariables(eq(stageUuid),  eq("key1"), eq("key2"), eq("key3"));

        assertThat(valueCapture.getValue().size()).isEqualTo(3);
        assertThat(valueCapture.getValue().keySet()).containsOnly("key1", "key2", "key3");
        assertThat(valueCapture.getValue().values()).containsOnly("");

        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void updateCount_zeroValue() {
        String variableName = "testVariableName";
        int additive = 1;

        when(caseworkClient.getDataValue(caseUUID.toString(), variableName)).thenReturn("0");

        bpmnService.updateCount(caseUUID.toString(), variableName, additive);

        verify(caseworkClient).getDataValue(caseUUID.toString(), variableName);
        verify(caseworkClient).updateDataValue(caseUUID.toString(), variableName, "1");

        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void updateCount_nonZeroValue_negativeAdditive() {
        String variableName = "testVariableName";
        int additive = -3;

        when(caseworkClient.getDataValue(caseUUID.toString(), variableName)).thenReturn("5");

        bpmnService.updateCount(caseUUID.toString(), variableName, additive);

        verify(caseworkClient).getDataValue(caseUUID.toString(), variableName);
        verify(caseworkClient).updateDataValue(caseUUID.toString(), variableName, "2");

        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldUpdateDeadlineForStages() {

        String stageType1 = "stage_type1";
        String no_of_days_1 = "5";
        String stageType2 = "stage_type2";
        String no_of_days_2 = "10";

        ArgumentCaptor<Map<String, Integer>> valueCapture = ArgumentCaptor.forClass(Map.class);

        Map<String, Integer> expectedData = Map.of("stage_type1", 5, "stage_type2", 10);

        bpmnService.updateDeadlineForStages(
                caseUUID.toString(),
                stageUUID.toString(),
                stageType1,
                no_of_days_1,
                stageType2,
                no_of_days_2
        );

        verify(caseworkClient).updateDeadlineForStages(
                eq(caseUUID), eq(stageUUID), valueCapture.capture()
        );
        assertThat(valueCapture.getValue().size()).isEqualTo(2);
        assertThat(valueCapture.getValue()).isEqualTo(expectedData);
         verifyNoMoreInteractions(caseworkClient, camundaClient, infoClient);
    }

    @Test
    public void shouldUpdateAllocationNote() {

        UUID testCaseId = UUID.randomUUID();
        UUID testStageId = UUID.randomUUID();
        String testCaseNote = "Case note text";
        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);

        when(caseworkClient.createCaseNote(testCaseId, "CLOSE_CASE", testCaseNote)).thenReturn(testCaseId);

        bpmnService.updateAllocationNote(testCaseId.toString(), testStageId.toString(), testCaseNote, "CLOSE_CASE");

        verify(caseworkClient, times(1)).createCaseNote(testCaseId, "CLOSE_CASE", testCaseNote);
        verify(caseworkClient).createCaseNote(eq(testCaseId), eq("CLOSE_CASE"), valueCapture.capture());
        assertThat(valueCapture.getValue()).isEqualTo(testCaseNote);
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldNotUpdateAllocationNoteFromNullNote() {

        UUID testCaseId = UUID.randomUUID();
        UUID testStageId = UUID.randomUUID();
        String testCaseNote = null;

        bpmnService.updateAllocationNote(testCaseId.toString(), testStageId.toString(), testCaseNote, "CLOSE_CASE");

        verify(caseworkClient, times(0)).createCaseNote(any(), any(), any());
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldCreateCaseNote() {

        UUID testCaseId = UUID.randomUUID();
        String testCaseNote = "Case note for closing a case by telephone.";
        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);

        when(caseworkClient.createCaseNote(testCaseId, "CLOSE_CASE_TELEPHONE", testCaseNote)).thenReturn(testCaseId);

        bpmnService.createCaseNote(testCaseId.toString(), testCaseNote, "CLOSE_CASE_TELEPHONE");

        verify(caseworkClient, times(1)).createCaseNote(testCaseId, "CLOSE_CASE_TELEPHONE", testCaseNote);
        verify(caseworkClient).createCaseNote(eq(testCaseId), eq("CLOSE_CASE_TELEPHONE"), valueCapture.capture());
        assertThat(valueCapture.getValue()).isEqualTo(testCaseNote);
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldCalculateDeadline() {

        //given
        String caseType = "FOI";
        int workingDays = 2;
        LocalDate localDate = LocalDate.of(1989, 01, 13);
        Clock fixedClock = Clock.fixed(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        Date dateReturnedByInfo = mock(Date.class);
        when(caseworkClient.calculateDeadline(caseType, localDate, workingDays)).thenReturn(dateReturnedByInfo);

        //when
        Date date = bpmnService.calculateDeadline(caseType, workingDays);

        //then
        assertThat(date).isEqualTo(dateReturnedByInfo);
    }

    @Test
    public void shouldAllocateUserToStage() {

        UUID caseUUID  = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();

        bpmnService.allocateUserToStage(caseUUID.toString(), stageUUID.toString(), userUUID.toString());

        verify(caseworkClient, times(1)).updateStageUser(caseUUID, stageUUID, userUUID);
        verify(caseworkClient).updateStageUser(eq(caseUUID), eq(stageUUID), eq(userUUID));

        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void shouldSetTodaysDate() {
        LocalDate localDate = LocalDate.of(1989, 01, 13);
        Clock fixedClock = Clock.fixed(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();

        UUID caseUUID  = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();

        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        bpmnService.saveTodaysDateToCaseVariable(caseUUID.toString(), stageUUID.toString(), "destination");

        verify(caseworkClient, times(1)).updateCase(eq(caseUUID), eq(stageUUID), valueCapture.capture());
        assertThat(valueCapture.getValue().size()).isEqualTo(1);
        assertThat(valueCapture.getValue().keySet()).contains("destination");
        assertThat(valueCapture.getValue().values()).contains("1989-01-13");
         verifyNoMoreInteractions(caseworkClient);
         verifyNoMoreInteractions(camundaClient);
         verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldAddCaseNoteOfAllocationDetails() {
        // GIVEN
        String testAllocationStageUUID = "1b61dc8b-6843-4d5d-9828-7a82be5f475b";
        String testNewTeamUUID = "833de531-446e-47c3-b021-691711f093ee";
        String testCaseUUID = "470852ed-654e-4974-b4e4-f255f470beb0";
        UUID mockCaseworkClientTeamResponse = UUID.randomUUID();
        String mockExistingTeamNameResponse = "MOCK EXISTING TEAM";
        String mockNewTeamNameResponse = "MOCK NEW TEAM NAME";
        TeamDto mockInfoClientExistingTeamResponse = new TeamDto(mockExistingTeamNameResponse, null, true, null);
        TeamDto mockInfoClientNewTeamResponse = new TeamDto(mockNewTeamNameResponse, null, true, null);

        String mockCaseWorkType = "test_type";
        StageTypeDto mockStageDtoResponse = new StageTypeDto(null, null, "MOCK STAGE NAME", mockCaseWorkType, null);

        when(caseworkClient.getStageTeam(UUID.fromString(testCaseUUID), UUID.fromString(testAllocationStageUUID))).thenReturn(mockCaseworkClientTeamResponse);
        when(infoClient.getTeam(mockCaseworkClientTeamResponse)).thenReturn(mockInfoClientExistingTeamResponse);
        when(caseworkClient.getStageType(UUID.fromString(testCaseUUID), UUID.fromString(testAllocationStageUUID))).thenReturn(mockCaseWorkType);
        when(infoClient.getAllStageTypes()).thenReturn(Set.of(mockStageDtoResponse));
        when(infoClient.getTeam(UUID.fromString(testNewTeamUUID))).thenReturn(mockInfoClientNewTeamResponse);

        String expectedCaseNote = mockExistingTeamNameResponse +
                " allocated case to " + mockNewTeamNameResponse + " at " + mockStageDtoResponse.getDisplayName() + " stage.";

        // WHEN
        bpmnService.createAllocationDetailsNote(testCaseUUID, testNewTeamUUID, testAllocationStageUUID);

        // THEN
        verify(caseworkClient, times(1)).createCaseNote(UUID.fromString(testCaseUUID), NoteType.ALLOCATE.toString(), expectedCaseNote);
    }
}
