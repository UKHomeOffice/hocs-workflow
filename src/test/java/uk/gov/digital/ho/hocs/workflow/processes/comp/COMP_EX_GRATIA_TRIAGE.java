package uk.gov.digital.ho.hocs.workflow.processes.comp;

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
@Deployment(resources = { "processes/COMP/COMP_EXGRATIA_TRIAGE.bpmn", "processes/COMP/COMP_CLOSE.bpmn" })
public class COMP_EX_GRATIA_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.85).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario exGratiaTriageProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testRejectTriage() {
        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Accept")).thenReturn(
            task -> task.complete(withVariables("valid", false))).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctTriageAccept", "No")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Transfer")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(
                withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_TriageTransfer", "Reject")));

        Scenario.run(exGratiaTriageProcess).startByKey("COMP_EXGRATIA_TRIAGE").execute();

        verify(exGratiaTriageProcess, times(3)).hasCompleted("Screen_Accept");
        verify(exGratiaTriageProcess, times(3)).hasCompleted("Screen_Transfer");
        verify(exGratiaTriageProcess).hasCompleted("Service_UpdateAllocationNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Reject"), eq("REJECT"));
    }

    @Test
    public void testTriageResultDraft() {
        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Accept")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Category")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Details")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_BusArea")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(
                withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "Pending"))).thenReturn(
            task -> task.complete(
                withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult",
                    "Draft")));

        Scenario.run(exGratiaTriageProcess).startByKey("COMP_EXGRATIA_TRIAGE").execute();

        verify(exGratiaTriageProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
    }

    @Test
    public void testTriageResultEscalate() {
        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Accept")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Category")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Details")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_BusArea")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Input")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult",
                "Escalate")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Escalate")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(
                withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_TriageEscalate", "Escalate")));

        Scenario.run(exGratiaTriageProcess).startByKey("COMP_EXGRATIA_TRIAGE").execute();

        verify(exGratiaTriageProcess).hasCompleted("Service_UpdateAllocationNote_Escalate");
        verify(exGratiaTriageProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Escalate");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Escalate"), eq("SEND_TO_WORKFLOW_MANAGER"));
    }

    @Test
    public void testTriageResultComplete() {

        whenAtCallActivity("COMP_CLOSE").thenReturn("CloseResult", "Yes", "varx", "vary", "DIRECTION",
            "BACKWARD").thenReturn("CloseResult", "Yes", "varx", "vary", "DIRECTION", "FORWARD").deploy(rule);

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Accept")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Category")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Details")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_BusArea")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Input")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult",
                "Complete")));

        Scenario.run(exGratiaTriageProcess).startByKey("COMP_EXGRATIA_TRIAGE").execute();

        verify(exGratiaTriageProcess, times(1)).hasCompleted("Activity_1d48rcf");
        verify(exGratiaTriageProcess, times(1)).hasCompleted("Screen_Details");
        verify(exGratiaTriageProcess, times(1)).hasCompleted("Screen_BusArea");
        verify(exGratiaTriageProcess, times(2)).hasCompleted("Screen_Input");

    }

    @Test
    public void testTriageAcceptCch() {
        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Accept")).thenReturn(
            task -> task.complete(withVariables("valid", false))).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctTriageAccept", "CCH")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Transfer")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(
                withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_TriageTransfer", "Reject")));

        Scenario.run(exGratiaTriageProcess).startByKey("COMP_EXGRATIA_TRIAGE").execute();

        verify(exGratiaTriageProcess, times(3)).hasCompleted("Screen_Accept");
        verify(exGratiaTriageProcess, times(3)).hasCompleted("Screen_Transfer");
        verify(exGratiaTriageProcess).hasCompleted("Service_UpdateAllocationNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Reject"), eq("REJECT"));
    }

    @Test
    public void testTriageAcceptOldValue() {
        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Accept")).thenReturn(
            task -> task.complete(withVariables("valid", false))).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctTriageAccept", "No")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Transfer")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(
                withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_TriageTransfer", "Reject")));

        Scenario.run(exGratiaTriageProcess).startByKey("COMP_EXGRATIA_TRIAGE").execute();

        verify(exGratiaTriageProcess, times(3)).hasCompleted("Screen_Accept");
        verify(exGratiaTriageProcess, times(3)).hasCompleted("Screen_Transfer");
        verify(exGratiaTriageProcess).hasCompleted("Service_UpdateAllocationNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Reject"), eq("REJECT"));
    }

    @Test
    public void testTriageAcceptPsu() {

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Accept")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctTriageAccept", "PSU")));

        when(exGratiaTriageProcess.waitsAtUserTask("Activity_ScreenCategorySerious")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(exGratiaTriageProcess).startByKey("COMP_EXGRATIA_TRIAGE").execute();

        verify(exGratiaTriageProcess, times(2)).hasCompleted("Screen_Accept");
        verify(exGratiaTriageProcess, times(3)).hasCompleted("Activity_ScreenCategorySerious");

    }

    @Test
    public void testTriageResultPsu() {

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Accept")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Category")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Details")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_BusArea")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(exGratiaTriageProcess.waitsAtUserTask("Validate_Input")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "PSU")));

        when(exGratiaTriageProcess.waitsAtUserTask("Activity_ScreenCategorySerious")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(exGratiaTriageProcess).startByKey("COMP_EXGRATIA_TRIAGE").execute();

        verify(exGratiaTriageProcess, times(1)).hasCompleted("Screen_Accept");
        verify(exGratiaTriageProcess, times(1)).hasCompleted("Activity_ScreenCategorySerious");

    }

}
