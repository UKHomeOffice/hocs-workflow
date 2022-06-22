package uk.gov.digital.ho.hocs.workflow.processes.comp;

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
@Deployment(resources = "processes/COMP/COMP_CLOSED.bpmn")
public class COMP_CLOSED {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario compClosedProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testNeitherCompleteOrTriage(){
        when(compClosedProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClosedCompType", "Rejected")));

        Scenario.run(compClosedProcess).startByKey("COMP_CLOSED").execute();

        verify(compClosedProcess, times(2)).hasCompleted("Screen_Input");
        verify(compClosedProcess).hasCompleted("ServiceTask_09n7cty");
    }

    @Test
    public void testRequiresTriage(){
        when(compClosedProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClosedCompType", "Service")));

        Scenario.run(compClosedProcess).startByKey("COMP_CLOSED").execute();

        verify(compClosedProcess).hasCompleted("Screen_Input");
        verify(compClosedProcess).hasCompleted("ServiceTask_09n7cty");
        verify(compClosedProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_CompServiceTriage");
    }

    @Test
    public void testClosedComplete(){
        when(compClosedProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClosedCompType", "Complete")));

        when(compClosedProcess.waitsAtUserTask("Validate_CompleteReason"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compClosedProcess.waitsAtUserTask("Validate_CompleteConfirm"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));

        Scenario.run(compClosedProcess).startByKey("COMP_CLOSED").execute();

        verify(compClosedProcess).hasCompleted("Screen_Input");
        verify(compClosedProcess, times(2)).hasCompleted("Screen_CompleteReason");
        verify(compClosedProcess, times(2)).hasCompleted("Screen_CompleteConfirm");
        verify(compClosedProcess).hasCompleted("Service_UpdateAllocationNote_Complete");
    }

    @Test
    public void testRestartAfterValidateCompleteReason(){
        when(compClosedProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClosedCompType", "Complete")));

        when(compClosedProcess.waitsAtUserTask("Validate_CompleteReason"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compClosedProcess.waitsAtUserTask("Validate_CompleteConfirm"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));

        Scenario.run(compClosedProcess).startByKey("COMP_CLOSED").execute();

        verify(compClosedProcess, times(2)).hasCompleted("Screen_Input");
        verify(compClosedProcess, times(2)).hasCompleted("Screen_CompleteReason");
        verify(compClosedProcess).hasCompleted("Screen_CompleteConfirm");
        verify(compClosedProcess).hasCompleted("Service_UpdateAllocationNote_Complete");
    }

    @Test
    public void testRestartAfterValidateConfirmComplete(){
        when(compClosedProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClosedCompType", "Complete")));

        when(compClosedProcess.waitsAtUserTask("Validate_CompleteReason"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compClosedProcess.waitsAtUserTask("Validate_CompleteConfirm"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD", "CompleteResult", "Yes")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));


        Scenario.run(compClosedProcess).startByKey("COMP_CLOSED").execute();

        verify(compClosedProcess).hasCompleted("Screen_Input");
        verify(compClosedProcess, times(2)).hasCompleted("Screen_CompleteReason");
        verify(compClosedProcess, times(2)).hasCompleted("Screen_CompleteConfirm");
        verify(compClosedProcess).hasCompleted("Service_UpdateAllocationNote_Complete");
    }

    @Test
    public void testNoCompleteResult(){
        when(compClosedProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "ClosedCompType", "Complete")));

        when(compClosedProcess.waitsAtUserTask("Validate_CompleteReason"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compClosedProcess.waitsAtUserTask("Validate_CompleteConfirm"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "No")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));


        Scenario.run(compClosedProcess).startByKey("COMP_CLOSED").execute();

        verify(compClosedProcess).hasCompleted("Screen_Input");
        verify(compClosedProcess, times(2)).hasCompleted("Screen_CompleteReason");
        verify(compClosedProcess, times(2)).hasCompleted("Screen_CompleteConfirm");
        verify(compClosedProcess).hasCompleted("Service_UpdateAllocationNote_Complete");
    }
}
