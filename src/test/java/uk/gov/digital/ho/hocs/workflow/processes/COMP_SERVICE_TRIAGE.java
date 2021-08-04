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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP_SERVICE_TRIAGE.bpmn")
public class COMP_SERVICE_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario compServiceTriageProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testRejectTriage(){
        when(compServiceTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "No")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Transfer"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_TriageTransfer", "Reject")));

        Scenario.run(compServiceTriageProcess).startByKey("COMP_SERVICE_TRIAGE").execute();

        verify(compServiceTriageProcess, times(3)).hasCompleted("Screen_Accept");
        verify(compServiceTriageProcess, times(3)).hasCompleted("Screen_Transfer");
        verify(compServiceTriageProcess).hasCompleted("Service_UpdateAllocationNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Reject"), eq("REJECT"));
    }

    @Test
    public void testTriageResultDraft(){
        when(compServiceTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_BusArea"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult", "Draft")));

        Scenario.run(compServiceTriageProcess).startByKey("COMP_SERVICE_TRIAGE").execute();

        verify(compServiceTriageProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
    }

    @Test
    public void testTriageResultEscalate(){
        when(compServiceTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_BusArea"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult", "Escalate")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Escalate"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_TriageEscalate", "Escalate")));

        Scenario.run(compServiceTriageProcess).startByKey("COMP_SERVICE_TRIAGE").execute();

        verify(compServiceTriageProcess).hasCompleted("Service_UpdateAllocationNote_Escalate");
        verify(compServiceTriageProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Escalate");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Escalate"), eq("SEND_TO_WORKFLOW_MANAGER"));
    }

    @Test
    public void testTriageResultComplete(){
        when(compServiceTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctTriageAccept", "Yes")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Category"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Details"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_BusArea"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CctTriageResult", "NotPending", "CctTriageResult", "Complete")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_CompleteReason"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_CompleteConfirm"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "No")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes", "CaseNote_CompleteReason", "Complete")));

        Scenario.run(compServiceTriageProcess).startByKey("COMP_SERVICE_TRIAGE").execute();

        verify(compServiceTriageProcess).hasCompleted("Service_UpdateAllocationNote_Complete");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Complete"), eq("CLOSE"));
    }

}
