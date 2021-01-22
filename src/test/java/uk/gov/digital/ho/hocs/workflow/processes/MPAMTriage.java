package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM_TRIAGE.bpmn")
public class MPAMTriage {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.2)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void whenMinisterialChangedToOfficial_thenMinisterialValuesAreCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Ministerial",
                        "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToOfficial_0ai1ek7"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("Screen_ReferenceTypeToOfficial_0pxyggg");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToOfficial_07vhlhy");
        verify(processScenario).hasCompleted("Service_ClearMinisterialValues_05l3eed");
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote_1sobox0");
        verify(processScenario).hasFinished("EndEvent_MpamTriage");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Official"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
    }

    @Test
    public void whenOfficialChangedToMinisterial_thenMinisterialValuesAreNotCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Official",
                        "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial_0k42bt1"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("Screen_ReferenceTypeToMinisterial_1c5qr22");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToMinisterial_0ai6870");
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote_1sobox0");
        verify(processScenario).hasFinished("EndEvent_MpamTriage");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Ministerial"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService, never()).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
    }

    @Test
    public void whenPutOnCampaign_thenUpdateTeam_andClearRejected() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "TriageOutcome", "PutOnCampaign")));

        when(processScenario.waitsAtUserTask("Validate_RequestCampaign"))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", true)));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE")
                .execute();

        verify(processScenario, times(2)).hasCompleted("Service_ClearCampaignType");
        verify(bpmnService, times(2)).blankCaseValues(any(), any(), eq("CampaignType"));
        verify(processScenario, times(3)).hasCompleted("Screen_RequestCampaign");
        verify(processScenario).hasCompleted("Service_UpdateTeamForCampaign");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_CAMPAIGN"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasCompleted("Service_ClearRejected");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasFinished("EndEvent_MpamTriage");
    }

    @Test
    public void whenSendToDraft_thenUpdatesTeam_andClearsRejected() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "TriageOutcome", "SendToDraft")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("Service_UpdateTeamForDraft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasCompleted("Service_ClearRejected");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasFinished("EndEvent_MpamTriage");
    }
}
