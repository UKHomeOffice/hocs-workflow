package uk.gov.digital.ho.hocs.workflow.processes.comp;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/COMP/COMP_CCH.bpmn","processes/COMP/COMP_CCH_RETURNS.bpmn"})
public class COMP_CCH {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario compReturnProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testChcCompTypeService() {
        when(compReturnProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", false, "CchCompType", "Service"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "Service")));

        Scenario.run(compReturnProcess).startByKey("COMP_CCH_RETURNS").execute();

        verify(compReturnProcess, times(2)).hasCompleted("Screen_Input");
        verify(compReturnProcess).hasCompleted("ServiceTask_1ipast6");
        verify(compReturnProcess).hasCompleted("Activity_0mtedcn");
        verify(bpmnService, times(1)).updateValue(any(), any(), eq("CompType"), eq("Service"));
        verify(compReturnProcess, never()).hasCompleted("Service_CompleteReason");
    }

    @Test
    public void testChcCompTypeComplete() {
        when(compReturnProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "Complete")));

        when(compReturnProcess.waitsAtUserTask("Validate_CompleteReason")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compReturnProcess.waitsAtUserTask("Validate_CompleteConfirm")).thenReturn(task -> task.complete(
            withVariables("valid", false, "DIRECTION", "FORWARD", "CompleteResult", "Yes"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));

        Scenario.run(compReturnProcess).startByKey("COMP_CCH_RETURNS").execute();

        verify(compReturnProcess, never()).hasCompleted("ServiceTask_1ipast6");
        verify(compReturnProcess, times(2)).hasCompleted("Service_CompleteReason");
        verify(compReturnProcess, times(2)).hasCompleted("Service_CompleteConfirm");
    }

    @Test
    public void testRestartAfterReason() {
        when(compReturnProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "Complete"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "Service")));

        when(compReturnProcess.waitsAtUserTask("Validate_CompleteReason")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")));

        Scenario.run(compReturnProcess).startByKey("COMP_CCH_RETURNS").execute();

        verify(compReturnProcess, times(2)).hasCompleted("Screen_Input");
        verify(compReturnProcess).hasCompleted("ServiceTask_1ipast6");
        verify(compReturnProcess).hasCompleted("Service_CompleteReason");
    }

    @Test
    public void testRestartAfterConfirm() {
        when(compReturnProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "Complete")));

        when(compReturnProcess.waitsAtUserTask("Validate_CompleteReason")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compReturnProcess.waitsAtUserTask("Validate_CompleteConfirm")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "BACKWARD", "CompleteResult", "Yes"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));

        Scenario.run(compReturnProcess).startByKey("COMP_CCH_RETURNS").execute();

        verify(compReturnProcess).hasCompleted("Screen_Input");
        verify(compReturnProcess, times(2)).hasCompleted("Service_CompleteReason");
        verify(compReturnProcess, times(2)).hasCompleted("Validate_CompleteConfirm");
    }

    @Test
    public void testIncompleteResult() {
        when(compReturnProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "Complete")));

        when(compReturnProcess.waitsAtUserTask("Validate_CompleteReason")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(compReturnProcess.waitsAtUserTask("Validate_CompleteConfirm")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "No"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompleteResult", "Yes")));

        Scenario.run(compReturnProcess).startByKey("COMP_CCH_RETURNS").execute();

        verify(compReturnProcess).hasCompleted("Screen_Input");
        verify(compReturnProcess, times(2)).hasCompleted("Service_CompleteReason");
        verify(compReturnProcess, times(2)).hasCompleted("Validate_CompleteConfirm");
    }

    @Test
    public void testUnexpectedCchCompType() {
        //This passes but should it?
        when(compReturnProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "Toast")));

        Scenario.run(compReturnProcess).startByKey("COMP_CCH_RETURNS").execute();

        verify(compReturnProcess).hasCompleted("Screen_Input");
        verify(compReturnProcess, never()).hasCompleted("ServiceTask_1ipast6");
        verify(compReturnProcess, never()).hasCompleted("Service_CompleteReason");
    }

    @Test
    public void testSendToExGratia() {
        when(compReturnProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "Ex-Gratia")));

        Scenario.run(compReturnProcess).startByKey("COMP_CCH_RETURNS").execute();

        verify(compReturnProcess).hasCompleted("Screen_Input");
        verify(compReturnProcess).hasCompleted("Activity_1fcu4pc");
        verify(compReturnProcess).hasCompleted("Activity_0mtedcn");
        verify(bpmnService, times(1)).updateValue(any(), any(), eq("CompType"), eq("Ex-Gratia"));
    }

    @Test
    public void testSendToMinorMisconduct() {
        when(compReturnProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CchCompType", "MinorMisconduct")));

        Scenario.run(compReturnProcess).startByKey("COMP_CCH_RETURNS").execute();

        verify(compReturnProcess).hasCompleted("Screen_Input");
        verify(compReturnProcess).hasCompleted("Activity_05t6j35");
        verify(compReturnProcess).hasCompleted("Activity_0mtedcn");
        verify(bpmnService, times(1)).updateValue(any(), any(), eq("CompType"), eq("MinorMisconduct"));
    }

}
