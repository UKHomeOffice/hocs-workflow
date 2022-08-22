package uk.gov.digital.ho.hocs.workflow.processes.pogr2;

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

import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/POGR2/POGR2.bpmn",
        "processes/STAGE.bpmn"})
public class POGR2 {

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
    public void testHappyHmpoPath() {
        whenAtCallActivity("POGR2_REGISTRATION")
                .thenReturn("", "")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR2")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR2");
        verify(processScenario).hasCompleted("CallActivity_RegistrationStage");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_POGR2");
    }

    @Test
    public void testHappyGroPath() {
        whenAtCallActivity("POGR2_REGISTRATION")
                .thenReturn("", "")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR2")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR2");
        verify(processScenario).hasCompleted("CallActivity_RegistrationStage");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_POGR2");
    }

}
