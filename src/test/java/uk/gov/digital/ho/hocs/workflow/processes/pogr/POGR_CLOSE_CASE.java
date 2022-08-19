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
@Deployment(resources = {
        "processes/POGR/SHARED/POGR_CLOSE_CASE.bpmn"
})
public class POGR_CLOSE_CASE {

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
        when(processScenario.waitsAtUserTask("Screen_CloseCase"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "ClosureCaseNote", "Test")));

        Scenario.run(processScenario)
                .startByKey("POGR_CLOSE_CASE")
                .execute();

        verify(processScenario).hasCompleted("Screen_CloseCase");

        verify(bpmnService).createCaseNote(any(), eq("Test"), eq("CLOSE"));
    }

    @Test
    public void testBackwardsDirection() {
        when(processScenario.waitsAtUserTask("Screen_CloseCase"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")));

        Scenario.run(processScenario)
                .startByKey("POGR_CLOSE_CASE")
                .execute();

        verify(processScenario, times(2)).hasCompleted("Screen_CloseCase");

        verify(bpmnService, times(0)).createCaseNote(any(), any(), eq("CLOSE"));
    }

    @Test
    public void testFowardsWithoutNoteDirection() {
        when(processScenario.waitsAtUserTask("Screen_CloseCase"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "ClosureCaseNote", "Test")));

        Scenario.run(processScenario)
                .startByKey("POGR_CLOSE_CASE")
                .execute();

        verify(processScenario, times(2)).hasCompleted("Screen_CloseCase");

        verify(bpmnService).createCaseNote(any(), eq("Test"), eq("CLOSE"));
    }


}
