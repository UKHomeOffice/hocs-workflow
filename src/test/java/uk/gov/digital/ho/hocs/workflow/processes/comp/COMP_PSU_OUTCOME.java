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

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/COMP/COMP_PSU_OUTCOME.bpmn"})
public class COMP_PSU_OUTCOME {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

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
        when(processScenario.waitsAtUserTask("Screen_ComplaintOutcome")).thenReturn(
            task -> task.complete(withVariables("PsuComplaintOutcome", "Substantiated")));

        when(processScenario.waitsAtUserTask("Screen_FinalResponse")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("COMP_PSU_OUTCOME")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Outcome");
        verify(processScenario).hasCompleted("Service_UpdatePsuDeadline");
        verify(bpmnService).updateDeadlineDays(any(), any(), eq("60"));
        verify(processScenario, times(2)).hasCompleted("Screen_ComplaintOutcome");
        verify(processScenario, times(3)).hasCompleted("Screen_FinalResponse");
        verify(processScenario).hasCompleted("EndEvent_Outcome");
    }

    @Test
    public void testReturnCase() {
        when(processScenario.waitsAtUserTask("Screen_ComplaintOutcome")).thenReturn(
            task -> task.complete(withVariables("PsuComplaintOutcome", "ReturnCase")));

        Scenario.run(processScenario)
            .startByKey("COMP_PSU_OUTCOME")
            .execute();

        verify(processScenario).hasCompleted("StartEvent_Outcome");
        verify(processScenario, times(1)).hasCompleted("Screen_ComplaintOutcome");
        verify(processScenario).hasCompleted("EndEvent_Outcome");
    }

    @Test
    public void testWithdrawCase() {
        when(processScenario.waitsAtUserTask("Screen_ComplaintOutcome")).thenReturn(
            task -> task.complete(withVariables("PsuComplaintOutcome", "Withdrawn")));

        Scenario.run(processScenario)
            .startByKey("COMP_PSU_OUTCOME")
            .execute();

        verify(processScenario).hasCompleted("StartEvent_Outcome");
        verify(processScenario, times(1)).hasCompleted("Screen_ComplaintOutcome");
        verify(processScenario, times(1)).hasCompleted("Activity_SaveWithdrawnNote");
        verify(processScenario).hasCompleted("EndEvent_Outcome");
    }
}
