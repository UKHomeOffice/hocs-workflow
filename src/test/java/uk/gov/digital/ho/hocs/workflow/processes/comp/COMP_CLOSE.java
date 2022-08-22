package uk.gov.digital.ho.hocs.workflow.processes.comp;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP/COMP_CLOSE.bpmn")
public class COMP_CLOSE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testDefaultRoute() {
        when(processScenario.waitsAtUserTask("Validate_CompleteReason"))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                                                  "CaseNote_CompleteReason","Complete",
                                                                     "DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startByKey("COMP_CLOSE").execute();

        verify(bpmnService).updateAllocationNote(any(), any(), eq("Complete"), eq("CLOSE"));
    }

    @Test
    public void testBackwards() {
        when(processScenario.waitsAtUserTask("Validate_CompleteReason"))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                                                  "CaseNote_CompleteReason","Complete",
                                                                     "DIRECTION", "BACKWARD")));

        Scenario.run(processScenario).startByKey("COMP_CLOSE").execute();

        verify(bpmnService, never()).updateAllocationNote(any(), any(), eq("Complete"), eq("CLOSE"));
    }

}
