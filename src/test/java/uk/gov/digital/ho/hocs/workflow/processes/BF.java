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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/BF.bpmn",
        "processes/BF_REGISTRATION.bpmn",
        "processes/BF_TRIAGE.bpmn",
        "processes/BF_SEND.bpmn",
        "processes/BF_ESCALATE.bpmn",
        "processes/BF_QA.bpmn",
        "processes/STAGE.bpmn"})
public class BF {

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
        whenAtCallActivity("BF_REGISTRATION")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("BF_TRIAGE")
                .thenReturn("valid", "true", "TriageResult", "Draft")
                .deploy(rule);

        whenAtCallActivity("BF_DRAFT")
                .thenReturn("valid", "true", "BfDraftResult", "Send")
                .deploy(rule);

        whenAtCallActivity("BF_SEND")
                .thenReturn("valid", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("BF")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BF");
        verify(processScenario).hasCompleted("CallActivity_BF_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_BF_TRIAGE");
        verify(processScenario).hasCompleted("CallActivity_BF_DRAFT");
        verify(processScenario).hasCompleted("CallActivity_BF_SEND");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_BF");

        verify(processScenario, times(0)).hasCompleted("CallActivity_BF_ESCALATE");
    }

    @Test
    public void testEscalate() {
        whenAtCallActivity("BF_REGISTRATION")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("BF_TRIAGE")
                .thenReturn("valid", "true", "TriageResult", "Escalate")
                .deploy(rule);

        whenAtCallActivity("BF_ESCALATE")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("BF_DRAFT")
                .thenReturn("valid", "true", "BfDraftResult", "Send")
                .deploy(rule);

        whenAtCallActivity("BF_SEND")
                .thenReturn("valid", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("BF")
                .execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_BF");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_BF");
    }

    @Test
    public void testQA() {
        whenAtCallActivity("BF_REGISTRATION")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("BF_TRIAGE")
                .thenReturn("valid", "true", "TriageResult", "Draft")
                .deploy(rule);

        whenAtCallActivity("BF_QA")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("BF_DRAFT")
                .thenReturn("valid", "true", "BfDraftResult", "QA")
                .deploy(rule);

        whenAtCallActivity("BF_SEND")
                .thenReturn("valid", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("BF")
                .execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_BF");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_QA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_BF");
    }

    @Test
    public void testTriageComplete() {
        whenAtCallActivity("BF_REGISTRATION")
                .thenReturn("valid", "true")
                .deploy(rule);

        whenAtCallActivity("BF_TRIAGE")
                .thenReturn("valid", "true", "TriageResult", "Complete")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("BF")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BF");
        verify(processScenario).hasCompleted("CallActivity_BF_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_BF_TRIAGE");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_BF");
    }

}
