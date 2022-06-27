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
        "processes/POGR/POGR_GRO_QA.bpmn"
})
public class POGR_GRO_QA {

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
        when(processScenario.waitsAtUserTask("Screen_QaInput"))
                .thenReturn(task -> task.complete(withVariables("QaOutcome", "Accept")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_QA")
                .execute();

        verify(processScenario).hasCompleted("EndEvent_GroQa");
    }

    @Test
    public void testQaReject() {
        when(processScenario.waitsAtUserTask("Screen_QaInput"))
                .thenReturn(task -> task.complete(withVariables("QaOutcome", "Reject")))
                .thenReturn(task -> task.complete(withVariables("QaOutcome", "Reject")));

        when(processScenario.waitsAtUserTask("Screen_QaReject"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "CaseNote_QaReject", "Reject")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_QA")
                .execute();

        verify(bpmnService).updateAllocationNote(any(), any(), eq("Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("EndEvent_GroQa");
    }
}
