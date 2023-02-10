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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/BF2/BF2_ESCALATE.bpmn")
public class BF2_ESCALATE {

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
        when(process.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", false))).thenReturn(
            task -> task.complete(withVariables("valid", true, "BfEscalationResult", "SendToDraft")));

        Scenario.run(process).startByKey("BF2_ESCALATE").execute();

        verify(process, times(2)).hasCompleted("Validate_Input");
        verify(process).hasCompleted("EndEvent_BF2_ESCALATE");
    }

    @Test
    public void testEscalateToPSU() {
        when(process.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "BfEscalationResult", "PSU")));

        when(process.waitsAtUserTask("Activity_ScreenCategorySerious")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(process).startByKey("BF2_ESCALATE").execute();

        verify(process, times(2)).hasCompleted("Validate_Input");
        verify(process, times(3)).hasCompleted("Activity_ScreenCategorySerious");
        verify(process).hasCompleted("EndEvent_BF2_ESCALATE");
    }

}
