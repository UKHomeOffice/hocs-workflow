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
}