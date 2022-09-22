package uk.gov.digital.ho.hocs.workflow.processes.pogr;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/POGR/POGR_GRO_INVESTIGATION.bpmn" })
public class POGR_GRO_INVESTIGATION {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

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
    public void testHappyPath() {
        when(processScenario.waitsAtUserTask("Screen_InvestigationAcceptCase")).thenReturn(
            task -> task.complete(withVariables("InvestigationAccept", ""))).thenReturn(
            task -> task.complete(withVariables("InvestigationAccept", "Accept"))).thenReturn(
            task -> task.complete(withVariables("InvestigationAccept", "Accept")));

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN").thenReturn("DIRECTION", "BACKWARD").thenReturn(
            "DIRECTION", "").thenReturn("DIRECTION", "FORWARD", "InvestigationOutcome", "Pending").thenReturn(
            "DIRECTION", "FORWARD", "InvestigationOutcome", "Draft").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR_GRO_INVESTIGATION").execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO_INVESTIGATION");
        verify(processScenario, times(3)).hasCompleted("Screen_InvestigationAcceptCase");
        verify(processScenario, times(4)).hasCompleted("CallActivity_Investigation");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO_INVESTIGATION");
    }

    @Test
    public void testInvestigationExternalRejection() {
        when(processScenario.waitsAtUserTask("Screen_InvestigationAcceptCase")).thenReturn(
            task -> task.complete(withVariables("InvestigationAccept", "Reject")));

        when(processScenario.waitsAtUserTask("Screen_GroTransferScreen")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(task -> task.complete(
            withVariables("DIRECTION", "FORWARD", "InvestigationRejectOptions", "External",
                "CaseNote_InvestigationReject", "Test")));

        Scenario.run(processScenario).startByKey("POGR_GRO_INVESTIGATION").execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO_INVESTIGATION");
        verify(processScenario, times(2)).hasCompleted("Screen_InvestigationAcceptCase");
        verify(processScenario, times(3)).hasCompleted("Screen_GroTransferScreen");
        verify(processScenario).hasCompleted("Activity_RejectCaseNote");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO_INVESTIGATION");

        verify(bpmnService).updateAllocationNote(any(), any(), eq("Test"), eq("REJECT"));
        verify(bpmnService, times(0)).updateTeamByStageAndTexts(any(), any(), eq("POGR_GRO_INVESTIGATION"), any(),
            any(), any());
    }

    @Test
    public void testInvestigationInternalRejection() {
        when(processScenario.waitsAtUserTask("Screen_InvestigationAcceptCase")).thenReturn(
            task -> task.complete(withVariables("InvestigationAccept", "Reject")));

        when(processScenario.waitsAtUserTask("Screen_GroTransferScreen")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(task -> task.complete(
            withVariables("DIRECTION", "FORWARD", "InvestigationRejectOptions", "Internal",
                "CaseNote_InvestigationReject", "Test")));

        Scenario.run(processScenario).startByKey("POGR_GRO_INVESTIGATION").execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO_INVESTIGATION");
        verify(processScenario, times(2)).hasCompleted("Screen_InvestigationAcceptCase");
        verify(processScenario, times(2)).hasCompleted("Screen_GroTransferScreen");
        verify(processScenario).hasCompleted("Activity_RejectCaseNote");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO_INVESTIGATION");

        verify(bpmnService).updateAllocationNote(any(), any(), eq("Test"), eq("REJECT"));
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("POGR_GRO_INVESTIGATION"), eq("DraftTeamUUID"),
            eq("DraftTeamName"), eq("InvestigationRejectGroInvestigatingTeamSelection"));
    }

    @Test
    public void testInvestigationEscalate() {
        when(processScenario.waitsAtUserTask("Screen_InvestigationAcceptCase")).thenReturn(
            task -> task.complete(withVariables("InvestigationAccept", "Accept")));

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN").thenReturn("DIRECTION", "FORWARD", "InvestigationOutcome",
            "Escalate").thenReturn("DIRECTION", "FORWARD", "InvestigationOutcome", "Escalate").deploy(rule);

        when(processScenario.waitsAtUserTask("Screen_GroEscalateScreen")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CaseNote_InvestigationEscalate", "Test")));

        Scenario.run(processScenario).startByKey("POGR_GRO_INVESTIGATION").execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO_INVESTIGATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_Investigation");
        verify(processScenario, times(3)).hasCompleted("Screen_GroEscalateScreen");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO_INVESTIGATION");

        verify(bpmnService).updateAllocationNote(any(), any(), eq("Test"), eq("SEND_TO_WORKFLOW_MANAGER"));

    }

    @Test
    public void testCloseCase() {
        when(processScenario.waitsAtUserTask("Screen_InvestigationAcceptCase")).thenReturn(
            task -> task.complete(withVariables("InvestigationAccept", "Accept")));

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN").thenReturn("DIRECTION", "FORWARD", "InvestigationOutcome",
            "Complete").thenReturn("DIRECTION", "FORWARD", "InvestigationOutcome", "Complete").deploy(rule);

        whenAtCallActivity("POGR_CLOSE_CASE").thenReturn("CloseCase", Boolean.toString(false)).thenReturn("CloseCase",
            Boolean.toString(true)).deploy(rule);

        Scenario.run(processScenario).startByKey("POGR_GRO_INVESTIGATION").execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO_INVESTIGATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_Investigation");
        verify(processScenario, times(2)).hasCompleted("CallActivity_InvestigationCloseCase");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO_INVESTIGATION");
    }

}
