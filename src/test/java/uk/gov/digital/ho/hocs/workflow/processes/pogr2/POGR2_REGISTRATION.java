package uk.gov.digital.ho.hocs.workflow.processes.pogr2;

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
@Deployment(resources = { "processes/POGR2/POGR2_REGISTRATION.bpmn" })
public class POGR2_REGISTRATION {

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
    public void testHmpoHappyPath() {
        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "BACKWARD")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR2_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Registration");
        verify(processScenario, times(2)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario).hasCompleted("EndEvent_Registration");
    }

    @Test
    public void testHappyGroPath() {
        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "BACKWARD")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR2_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Registration");
        verify(processScenario, times(2)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario).hasCompleted("EndEvent_Registration");
    }
}
