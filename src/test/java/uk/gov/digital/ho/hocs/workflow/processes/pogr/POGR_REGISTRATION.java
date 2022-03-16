package uk.gov.digital.ho.hocs.workflow.processes.pogr;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/POGR/POGR_REGISTRATION.bpmn" })
public class POGR_REGISTRATION {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(0.1).build();

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
    public void testHmpoHappyPath() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO")));

        when(processScenario.waitsAtUserTask("Screen_Hmpo_DataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BusinessSelect");
        verify(processScenario, times(2)).hasCompleted("Screen_BusinessAreaSelect");
        verify(processScenario, times(2)).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario, times(2)).hasCompleted("Screen_Hmpo_DataInput");
        verify(processScenario).hasCompleted("EndEvent_BusinessSelect");

        verify(bpmnService, times(2)).updateDeadlineDays(any(), any(), eq("10"));
    }

    @Test
    public void testGroHappyPath() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO")));

        when(processScenario.waitsAtUserTask("Screen_Gro_DataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BusinessSelect");
        verify(processScenario, times(2)).hasCompleted("Screen_BusinessAreaSelect");
        verify(processScenario, times(2)).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario, times(2)).hasCompleted("Screen_Gro_DataInput");
        verify(processScenario).hasCompleted("EndEvent_BusinessSelect");

        verify(bpmnService, times(2)).updateDeadlineDays(any(), any(), eq("5"));

    }

    @Test
    public void testSwapBetweenBusinessAreas() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO")));

        when(processScenario.waitsAtUserTask("Screen_Gro_DataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")));

        when(processScenario.waitsAtUserTask("Screen_Hmpo_DataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BusinessSelect");
        verify(processScenario, times(2)).hasCompleted("Screen_BusinessAreaSelect");
        verify(processScenario, times(2)).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario).hasCompleted("Screen_Gro_DataInput");
        verify(processScenario).hasCompleted("Screen_Hmpo_DataInput");
        verify(processScenario).hasCompleted("EndEvent_BusinessSelect");

        verify(bpmnService).updateDeadlineDays(any(), any(), eq("5"));
        verify(bpmnService).updateDeadlineDays(any(), any(), eq("10"));
    }


}
