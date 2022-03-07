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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/BF2_TRIAGE.bpmn")
public class BF2_TRIAGE {

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
                .thenReturn(task -> task.complete(withVariables("valid", false, "BfTriageAccept", "Yes")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Contributions"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "BFTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BFTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BFTriageResult", "Draft")));

        Scenario.run(process).startByKey("BF2_TRIAGE").execute();
        verify(process).hasCompleted("EndEvent_BF2_TRIAGE");
    }

    @Test
    public void testTriageReject(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "BfTriageAccept", "No")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BfTriageAccept", "No")));

        Scenario.run(process).startByKey("BF2_TRIAGE").execute();
        verify(process).hasCompleted("EndEvent_BF2_TRIAGE");
    }

    @Test
    public void testValidateContributionsBackThenComplete(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Contributions"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BFTriageResult", "Draft")));

        Scenario.run(process).startByKey("BF2_TRIAGE").execute();
        verify(process).hasCompleted("EndEvent_BF2_TRIAGE");
    }

    @Test
    public void testCompleteComplaint(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Contributions"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BFTriageResult", "Complete")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BFTriageResult", "Complete")));

        when(process.waitsAtUserTask("Validate_Complete_Reason"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        Scenario.run(process).startByKey("BF2_TRIAGE").execute();
        verify(process).hasCompleted("EndEvent_BF2_TRIAGE");
    }

    @Test
    public void testEscalate(){
        when(process.waitsAtUserTask("Validate_Accept_Case"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Contributions"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BFTriageResult", "Escalate")));

        when(process.waitsAtUserTask("Validate_Escalate"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "BFTriageResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        Scenario.run(process).startByKey("BF2_TRIAGE").execute();
        verify(process).hasCompleted("Save_Note");
        verify(process).hasCompleted("EndEvent_BF2_TRIAGE");
    }
}
