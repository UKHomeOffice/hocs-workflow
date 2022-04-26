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
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.BACKWARD;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.DIRECTION;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.FORWARD;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.VALID;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/BF_TRIAGE.bpmn")
public class BF_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario process;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testHappyPath(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables(VALID, false, "BfTriageAccept", "Yes")))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason"))
                .thenReturn(task -> task.complete(withVariables(VALID, false, DIRECTION, BACKWARD)))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)));

        when(process.waitsAtUserTask("Validate_Contributions"))
                .thenReturn(task -> task.complete(withVariables(VALID, false, "BFTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BFTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BFTriageResult", "Draft")));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testTriageReject(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables(VALID, false, "BfTriageAccept", "No")))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "No")));

        when(process.waitsAtUserTask("Transfer_Case"))
                .thenReturn(task -> task.complete(withVariables(VALID, false, DIRECTION, FORWARD)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, BACKWARD, VALID, false)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, VALID, true, "CaseNote_TriageTransfer", "Reject note")));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(1)).hasCompleted("EndEvent_BF_TRIAGE");
        verify(process, times(1)).hasCompleted("Save_Reject_Note");
        verify(bpmnService, times(1)).updateAllocationNote(any(), any(), eq("Reject note"), eq("REJECT"));
    }

    @Test
    public void testValidateContributionsBackThenComplete(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)));

        when(process.waitsAtUserTask("Validate_Contributions"))
                .thenReturn(task -> task.complete(withVariables(VALID, false, DIRECTION, BACKWARD)))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BFTriageResult", "Draft")));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testCompleteComplaint(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)));

        when(process.waitsAtUserTask("Validate_Contributions"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BFTriageResult", "Complete")))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BFTriageResult", "Complete")));

        when(process.waitsAtUserTask("Validate_Complete_Reason"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, BACKWARD)))
                .thenReturn(task -> task.complete(withVariables(VALID, false, DIRECTION, FORWARD)))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testEscalate(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)));

        when(process.waitsAtUserTask("Validate_Contributions"))
                .thenReturn(task -> task.complete(withVariables(VALID, true, "BFTriageResult", "Escalate")));

        when(process.waitsAtUserTask("Validate_Escalate"))
                .thenReturn(task -> task.complete(withVariables(VALID, false, "BFTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, BACKWARD)))
                .thenReturn(task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process).hasCompleted("Save_Note");
        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }
}
