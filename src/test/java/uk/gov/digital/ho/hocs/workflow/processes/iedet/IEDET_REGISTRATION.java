package uk.gov.digital.ho.hocs.workflow.processes.iedet;

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
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/IEDET/IEDET_REGISTRATION.bpmn")
public class IEDET_REGISTRATION {

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
        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "")
                .thenReturn("DIRECTION", "FORWARD")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        when(processScenario.waitsAtUserTask("Screen_ComplainantInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("IEDET_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Registration");
        verify(processScenario, times(3)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario, times(3)).hasCompleted("Screen_ComplainantInput");
        verify(processScenario).hasCompleted("EndEvent_Registration");
    }
}