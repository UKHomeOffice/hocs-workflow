package uk.gov.digital.ho.hocs.workflow.processes.bf2;

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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/BF2/BF2_SEND.bpmn")
public class BF2_SEND {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

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
    public void testHappyPath() {
        when(process.waitsAtUserTask("Validate_Send")).thenReturn(
            task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        Scenario.run(process).startByKey("BF2_SEND").execute();
        verify(process).hasCompleted("EndEvent_BF2_SEND");
    }

}
