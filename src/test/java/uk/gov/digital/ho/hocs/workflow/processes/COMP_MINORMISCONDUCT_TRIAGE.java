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

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP_MINORMISCONDUCT_TRIAGE.bpmn")
public class COMP_MINORMISCONDUCT_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario minorMisconductTriageProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testRejectTriage(){
        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "No")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Transfer"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_TriageTransfer", "Reject")));

        Scenario.run(minorMisconductTriageProcess).startByKey("COMP_MINORMISCONDUCT_TRIAGE").execute();

        verify(minorMisconductTriageProcess, times(3)).hasCompleted("Screen_Accept");
        verify(minorMisconductTriageProcess, times(3)).hasCompleted("Screen_Transfer");
        verify(minorMisconductTriageProcess).hasCompleted("Service_UpdateAllocationNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Reject"), eq("REJECT"));
    }

    @Test
    public void testTriageResultDraft(){
        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_BusArea"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult", "Draft")));

        Scenario.run(minorMisconductTriageProcess).startByKey("COMP_MINORMISCONDUCT_TRIAGE").execute();

        verify(minorMisconductTriageProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
    }

    @Test
    public void testTriageResultEscalate(){
        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_BusArea"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult", "Escalate")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Escalate"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_TriageEscalate", "Escalate")));

        Scenario.run(minorMisconductTriageProcess).startByKey("COMP_MINORMISCONDUCT_TRIAGE").execute();

        verify(minorMisconductTriageProcess).hasCompleted("Service_UpdateAllocationNote_Escalate");
        verify(minorMisconductTriageProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Escalate");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Escalate"), eq("SEND_TO_WORKFLOW_MANAGER"));
    }

    @Test
    public void testTriageResultComplete(){
        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_BusArea"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult", "Complete")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_CompleteReason"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(minorMisconductTriageProcess.waitsAtUserTask("Validate_CompleteConfirm"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "No")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes", "CaseNote_CompleteReason", "Complete")));

        Scenario.run(minorMisconductTriageProcess).startByKey("COMP_MINORMISCONDUCT_TRIAGE").execute();

        verify(minorMisconductTriageProcess).hasCompleted("Service_UpdateAllocationNote_Complete");
        verify(minorMisconductTriageProcess, times(2)).hasCompleted("Screen_Input");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Complete"), eq("CLOSE"));
    }
}
