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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/POGR/POGR_HMPO_TRIAGE.bpmn"
})
public class POGR_HMPO_TRIAGE {

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
        when(processScenario.waitsAtUserTask("Screen_TriageAcceptCase"))
                .thenReturn(task -> task.complete(withVariables("TriageAccept", "")))
                .thenReturn(task -> task.complete(withVariables("TriageAccept", "Accept")));

        when(processScenario.waitsAtUserTask("Screen_BlankPage"))
                .thenReturn(task -> task.complete(withVariables("TriageAccept", "Accept")));

        Scenario.run(processScenario)
                .startByKey("POGR_HMPO_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_HMPO_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("Screen_TriageAcceptCase");
        verify(processScenario).hasCompleted("Screen_BlankPage");
        verify(processScenario).hasCompleted("EndEvent_POGR_HMPO_TRIAGE");
    }

    @Test
    public void testRejectCase() {
        when(processScenario.waitsAtUserTask("Screen_TriageAcceptCase"))
                .thenReturn(task -> task.complete(withVariables("TriageAccept", "Reject")));

        Scenario.run(processScenario)
                .startByKey("POGR_HMPO_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_HMPO_TRIAGE");
        verify(processScenario).hasCompleted("Screen_TriageAcceptCase");
        verify(processScenario).hasCompleted("Activity_CloseCaseNote");
        verify(processScenario).hasCompleted("EndEvent_POGR_HMPO_TRIAGE");
    }

}
