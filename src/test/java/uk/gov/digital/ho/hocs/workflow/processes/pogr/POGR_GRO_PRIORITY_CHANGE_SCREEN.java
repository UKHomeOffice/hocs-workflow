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

import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/POGR/GRO/POGR_GRO_PRIORITY_CHANGE_SCREEN.bpmn" })
public class POGR_GRO_PRIORITY_CHANGE_SCREEN {

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
        when(processScenario.waitsAtUserTask("Screen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_PRIORITY_CHANGE_SCREEN", Map.of("SCREEN_KEY", "TEST"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_0");
        verify(processScenario).hasCompleted("Screen");
        verify(processScenario).hasCompleted("EndEvent_0");

        verify(processScenario, times(0)).hasCompleted("Service_UpdateCaseDeadline");
    }

    @Test
    public void testBackwardPath() {
        when(processScenario.waitsAtUserTask("Screen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_PRIORITY_CHANGE_SCREEN", Map.of("SCREEN_KEY", "TEST"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_0");
        verify(processScenario).hasCompleted("Screen");
        verify(processScenario).hasCompleted("EndEvent_0");

        verify(processScenario, times(0)).hasCompleted("Service_UpdateCaseDeadline");
    }

    @Test
    public void testChangedPriorityNoPath() {
        when(processScenario.waitsAtUserTask("Screen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "ComplaintPriority", "")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_PRIORITY_CHANGE_SCREEN", Map.of("SCREEN_KEY", "TEST", "ComplaintPriority", "Yes"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_0");
        verify(processScenario).hasCompleted("Screen");
        verify(processScenario).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario).hasCompleted("EndEvent_0");

        verify(bpmnService).updateDeadlineDays(any(), any(), eq("5"));
    }

    @Test
    public void testChangedPriorityYesPath() {
        when(processScenario.waitsAtUserTask("Screen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "ComplaintPriority", "Yes")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_PRIORITY_CHANGE_SCREEN", Map.of("SCREEN_KEY", "TEST", "ComplaintPriority", ""))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_0");
        verify(processScenario).hasCompleted("Screen");
        verify(processScenario).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario).hasCompleted("EndEvent_0");

        verify(bpmnService).updateDeadlineDays(any(), any(), eq("1"));
    }

    @Test
    public void testChangedChannelPostPath() {
        when(processScenario.waitsAtUserTask("Screen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "ComplaintChannel", "Post")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_PRIORITY_CHANGE_SCREEN", Map.of("SCREEN_KEY", "TEST", "ComplaintChannel", ""))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_0");
        verify(processScenario).hasCompleted("Screen");
        verify(processScenario).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario).hasCompleted("EndEvent_0");

        verify(bpmnService).updateDeadlineDays(any(), any(), eq("10"));
    }

    @Test
    public void testChangedChannelOtherPath() {
        when(processScenario.waitsAtUserTask("Screen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "ComplaintChannel", "Other")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_PRIORITY_CHANGE_SCREEN", Map.of("SCREEN_KEY", "TEST", "ComplaintChannel", ""))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_0");
        verify(processScenario).hasCompleted("Screen");
        verify(processScenario).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario).hasCompleted("EndEvent_0");

        verify(bpmnService).updateDeadlineDays(any(), any(), eq("5"));
    }

    @Test
    public void testNoChange() {
        when(processScenario.waitsAtUserTask("Screen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "ComplaintPriority", "Yes", "ComplaintChannel", "Post")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_PRIORITY_CHANGE_SCREEN", Map.of("SCREEN_KEY", "TEST", "ComplaintPriority", "Yes", "ComplaintChannel", "Post"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_0");
        verify(processScenario).hasCompleted("Screen");
        verify(processScenario, times(0)).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario).hasCompleted("EndEvent_0");

        verify(bpmnService, times(0)).updateDeadlineDays(any(), any(), any());
    }
}
