package uk.gov.digital.ho.hocs.workflow.processes;

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

import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/SMC.bpmn",
        "processes/SMC_REGISTRATION.bpmn",
        "processes/SMC_TRIAGE.bpmn",
        "processes/SMC_SEND.bpmn",
        "processes/STAGE.bpmn"})
public class SMC {

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
    public void testHappyPath() {
        whenAtCallActivity("SMC_REGISTRATION")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("SMC_TRIAGE")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("SMC_DRAFT")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("SMC_SEND")
                .thenReturn("valid", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("SMC")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_SMC");
        verify(processScenario).hasCompleted("CallActivity_SMC_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_SMC_TRIAGE");
        verify(processScenario).hasCompleted("CallActivity_SMC_DRAFT");
        verify(processScenario).hasCompleted("CallActivity_SMC_SEND");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_SMC");
    }

}
