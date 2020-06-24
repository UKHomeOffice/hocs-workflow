package uk.gov.digital.ho.hocs.workflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

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

    private BpmnService bpmnService;

    private String caseUUID = UUID.randomUUID().toString();

    private String stageUUID = UUID.randomUUID().toString();


    @Before
    public void setup() {
        bpmnService = new BpmnService(caseworkClient, camundaClient, infoClient);
    }

    @Test
    public void shoudCalculateTotals() {
        when(caseworkClient.calculateTotals(UUID.fromString(caseUUID), UUID.fromString(stageUUID), "list")).thenReturn(null);

        bpmnService.calculateTotals(caseUUID, stageUUID, "list");

        verify(caseworkClient).calculateTotals(UUID.fromString(caseUUID), UUID.fromString(stageUUID), "list");
        verifyNoMoreInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shoudUpdateDeadlineDays() {
        doNothing().when(caseworkClient).updateDeadlineDays(UUID.fromString(caseUUID), UUID.fromString(stageUUID), 123);

        bpmnService.updateDeadlineDays(caseUUID, stageUUID, "123");

        verify(caseworkClient).updateDeadlineDays(UUID.fromString(caseUUID), UUID.fromString(stageUUID), 123);
        verifyNoMoreInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shoudUpdateStageDeadline() {
        doNothing().when(caseworkClient).updateStageDeadline(UUID.fromString(caseUUID), UUID.fromString(stageUUID), "TEST", 7);

        bpmnService.updateStageDeadline(caseUUID, stageUUID, "TEST", "7");

        verify(caseworkClient).updateStageDeadline(UUID.fromString(caseUUID), UUID.fromString(stageUUID), "TEST", 7);
        verifyNoMoreInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldNotUpdateDataNoNewTeams() {
        bpmnService.updateTeamSelection(caseUUID, stageUUID, null, null);

        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldUpdateDataNewDraftingTeams() {

        UUID draftingString = UUID.randomUUID();

        when(infoClient.getTeam(draftingString)).thenReturn(new TeamDto("Team1", draftingString, true, new HashSet<>()));
        doNothing().when(camundaClient).updateTask(eq(UUID.fromString(stageUUID)), any());
        doNothing().when(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());

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
        doNothing().when(camundaClient).updateTask(eq(UUID.fromString(stageUUID)), any());
        doNothing().when(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());

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
        when(infoClient.getTeam(draftingString)).thenReturn(new TeamDto("Team1", privateOfficeString, true, new HashSet<>()));
        doNothing().when(camundaClient).updateTask(eq(UUID.fromString(stageUUID)), any());
        doNothing().when(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());

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
        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldUpdateValue() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        bpmnService.updateValue(caseUUID, stageUUID, "testKey", "testValue");

        verify(camundaClient, times(1)).updateTask(eq(UUID.fromString(stageUUID)), valueCapture.capture());
        verify(caseworkClient, times(1)).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), any());
        assertThat(valueCapture.getValue().size()).isEqualTo(1);
        assertThat(valueCapture.getValue().keySet()).contains("testKey");
        assertThat(valueCapture.getValue().values()).contains("testValue");
        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldUpdateValue_multiple() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);
        Map<String, String> expectedData = Map.of("key1", "value1", "key2", "value3", "key3", "value3");
        bpmnService.updateValue(caseUUID, stageUUID, "key1", "value1", "key2", "value3", "key3", "value3");

        verify(camundaClient).updateTask(eq(UUID.fromString(stageUUID)), valueCapture.capture());
        verify(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), eq(expectedData));
        assertThat(valueCapture.getValue().size()).isEqualTo(3);
        assertThat(valueCapture.getValue()).isEqualTo(expectedData);
        verifyZeroInteractions(caseworkClient, camundaClient, infoClient);
    }

    @Test(expected = ApplicationExceptions.InvalidMethodArgumentException.class)
    public void shouldFailToUpdateValue() {
        bpmnService.updateCaseValue(caseUUID, stageUUID, "key1", "value1", "key2", "value3", "key3", "value3", "key4");
    }

    @Test
    public void shouldUpdateCaseValue() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        bpmnService.updateCaseValue(caseUUID, stageUUID, "testKey", "testValue");

        verify(caseworkClient, times(1)).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), valueCapture.capture());
        assertThat(valueCapture.getValue().size()).isEqualTo(1);
        assertThat(valueCapture.getValue().keySet()).contains("testKey");
        assertThat(valueCapture.getValue().values()).contains("testValue");
        verifyZeroInteractions(caseworkClient);
        verifyZeroInteractions(camundaClient);
        verifyZeroInteractions(infoClient);
    }

    @Test
    public void shouldUpdateCaseValue_multiple() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        Map<String, String> expectedData = Map.of("key1", "value1", "key2", "value3", "key3", "value3");
        bpmnService.updateCaseValue(caseUUID, stageUUID, "key1", "value1", "key2", "value3", "key3", "value3");

        verify(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), valueCapture.capture());
        assertThat(valueCapture.getValue().size()).isEqualTo(3);
        assertThat(valueCapture.getValue()).isEqualTo(expectedData);
        verifyZeroInteractions(caseworkClient, camundaClient, infoClient);
    }

    @Test(expected = ApplicationExceptions.InvalidMethodArgumentException.class)
    public void shouldFailToUpdateCaseValue() {

        bpmnService.updateCaseValue(caseUUID, stageUUID, "key1", "value1", "key2", "value3", "key3", "value3", "key4");

    }

    @Test
    public void shouldBlankCaseValues() {
        ArgumentCaptor<Map<String, String>> valueCapture = ArgumentCaptor.forClass(Map.class);

        bpmnService.blankCaseValues(caseUUID, stageUUID, "key1", "key2", "key3");

        verify(caseworkClient).updateCase(eq(UUID.fromString(caseUUID)), eq(UUID.fromString(stageUUID)), valueCapture.capture());
        assertThat(valueCapture.getValue().size()).isEqualTo(3);
        assertThat(valueCapture.getValue().keySet()).containsOnly("key1", "key2", "key3");
        assertThat(valueCapture.getValue().values()).containsOnly("");
        verifyZeroInteractions(caseworkClient, camundaClient, infoClient);
    }

    @Test
    public void shouldCreateNewStage() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        String allocationTeam = UUID.randomUUID().toString();
        String allocatedUserId = UUID.randomUUID().toString();

        UUID expectedStageUUID = UUID.randomUUID();

        when(caseworkClient.createStage(UUID.fromString(caseUUID), stageType, UUID.fromString(allocationTeam), UUID.fromString(allocatedUserId), allocationType)).thenReturn(expectedStageUUID);
        String resultUUID = bpmnService.createStage(caseUUID, null, stageType, allocationType, allocationTeam, allocatedUserId);

        assertThat(resultUUID).isEqualTo(expectedStageUUID.toString());
        verify(caseworkClient).createStage(UUID.fromString(caseUUID), stageType, UUID.fromString(allocationTeam), UUID.fromString(allocatedUserId), allocationType);
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);

    }

    @Test
    public void shouldCreateNewStage_NoUserParam() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        String allocationTeam = UUID.randomUUID().toString();

        UUID expectedStageUUID = UUID.randomUUID();

        when(caseworkClient.createStage(UUID.fromString(caseUUID), stageType, UUID.fromString(allocationTeam), null, allocationType)).thenReturn(expectedStageUUID);
        String resultUUID = bpmnService.createStage(caseUUID, null, stageType, allocationType, allocationTeam);

        assertThat(resultUUID).isEqualTo(expectedStageUUID.toString());
        verify(caseworkClient).createStage(UUID.fromString(caseUUID), stageType, UUID.fromString(allocationTeam), null, allocationType);
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);

    }

    @Test
    public void shouldCreateNewStage_NoAllocationTeam() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        String allocatedUserId = UUID.randomUUID().toString();

        UUID expectedStageUUID = UUID.randomUUID();
        UUID expectedAllocationTeam = UUID.randomUUID();

        when(infoClient.getTeamForStageType(stageType)).thenReturn(expectedAllocationTeam);
        when(caseworkClient.createStage(UUID.fromString(caseUUID), stageType, expectedAllocationTeam, UUID.fromString(allocatedUserId), allocationType)).thenReturn(expectedStageUUID);
        String resultUUID = bpmnService.createStage(caseUUID, null, stageType, allocationType, null, allocatedUserId);

        assertThat(resultUUID).isEqualTo(expectedStageUUID.toString());
        verify(infoClient).getTeamForStageType(stageType);
        verify(caseworkClient).createStage(UUID.fromString(caseUUID), stageType, expectedAllocationTeam, UUID.fromString(allocatedUserId), allocationType);
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);

    }

    @Test
    public void shouldRecreateStage() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        String allocationTeam = UUID.randomUUID().toString();
        String allocatedUserId = UUID.randomUUID().toString();

        String resultUUID = bpmnService.createStage(caseUUID, stageUUID, stageType, allocationType, allocationTeam, allocatedUserId);

        assertThat(resultUUID).isEqualTo(stageUUID);
        verify(caseworkClient).recreateStage(UUID.fromString(caseUUID), UUID.fromString(stageUUID), stageType);
        verify(caseworkClient).updateStageTeam(UUID.fromString(caseUUID), UUID.fromString(stageUUID), UUID.fromString(allocationTeam), allocationType);
        verify(caseworkClient).updateStageUser(UUID.fromString(caseUUID), UUID.fromString(stageUUID), UUID.fromString(allocatedUserId));
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);

    }

    @Test
    public void shouldRecreateStage_NoUserUUIDProvided() {
        String stageType = "testStageType";
        String allocationType = "testAllocationType";
        String allocationTeam = UUID.randomUUID().toString();

        String resultUUID = bpmnService.createStage(caseUUID, stageUUID, stageType, allocationType, allocationTeam, null);

        assertThat(resultUUID).isEqualTo(stageUUID);
        verify(caseworkClient).recreateStage(UUID.fromString(caseUUID), UUID.fromString(stageUUID), stageType);
        verify(caseworkClient).updateStageTeam(UUID.fromString(caseUUID), UUID.fromString(stageUUID), UUID.fromString(allocationTeam), allocationType);
        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);

    }

    @Test
    public void updateCount_nullValue() {
        String variableName = "testVariableName";
        int additive = 1;

        when(caseworkClient.getDataValue(caseUUID, variableName)).thenReturn(null);

        bpmnService.updateCount(caseUUID, variableName, additive);

        verify(caseworkClient).getDataValue(caseUUID, variableName);
        verify(caseworkClient).updateDataValue(caseUUID, variableName, "1");

        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void updateCount_zeroValue() {
        String variableName = "testVariableName";
        int additive = 1;

        when(caseworkClient.getDataValue(caseUUID, variableName)).thenReturn("0");

        bpmnService.updateCount(caseUUID, variableName, additive);

        verify(caseworkClient).getDataValue(caseUUID, variableName);
        verify(caseworkClient).updateDataValue(caseUUID, variableName, "1");

        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }

    @Test
    public void updateCount_nonZeroValue_negativeAdditive() {
        String variableName = "testVariableName";
        int additive = -3;

        when(caseworkClient.getDataValue(caseUUID, variableName)).thenReturn("5");

        bpmnService.updateCount(caseUUID, variableName, additive);

        verify(caseworkClient).getDataValue(caseUUID, variableName);
        verify(caseworkClient).updateDataValue(caseUUID, variableName, "2");

        verifyNoMoreInteractions(caseworkClient, infoClient, camundaClient);
    }
}