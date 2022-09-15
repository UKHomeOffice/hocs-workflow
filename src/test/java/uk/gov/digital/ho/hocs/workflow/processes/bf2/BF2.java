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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/BF2/BF2.bpmn", "processes/BF2/BF2_REGISTRATION.bpmn",
    "processes/BF2/BF2_TRIAGE.bpmn", "processes/BF2/BF2_SEND.bpmn", "processes/BF2/BF2_ESCALATE.bpmn",
    "processes/BF2/BF2_QA.bpmn", "processes/STAGE.bpmn" })
public class BF2 {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.1).build();

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
        whenAtCallActivity("BF2_REGISTRATION").deploy(rule);

        whenAtCallActivity("BF2_TRIAGE").thenReturn("BFTriageResult", "Draft", "BfTriageAccept", "Yes").deploy(rule);

        whenAtCallActivity("BF2_DRAFT").thenReturn("BfDraftResult", "QA").deploy(rule);

        whenAtCallActivity("BF2_QA").thenReturn("BfQaResult", "Accept").deploy(rule);

        whenAtCallActivity("BF2_SEND").deploy(rule);

        Scenario.run(processScenario).startByKey("BF2").execute();

        verify(processScenario).hasCompleted("StartEvent_BF2");
        verify(processScenario).hasCompleted("CallActivity_BF2_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_BF2_TRIAGE");
        verify(processScenario).hasCompleted("CallActivity_BF2_DRAFT");
        verify(processScenario).hasCompleted("CallActivity_BF2_QA");
        verify(processScenario).hasCompleted("CallActivity_BF2_SEND");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_BF2");

        verify(processScenario, times(0)).hasCompleted("CallActivity_BF2_ESCALATE");
    }

    @Test
    public void testEscalate() {
        whenAtCallActivity("BF2_REGISTRATION").deploy(rule);

        whenAtCallActivity("BF2_TRIAGE").thenReturn("BFTriageResult", "Escalate", "BfTriageAccept", "Yes").thenReturn(
            "BFTriageResult", "Escalate", "BfTriageAccept", "Yes").deploy(rule);

        whenAtCallActivity("BF2_ESCALATE").thenReturn("BfEscalationResult", "SendToTriage").thenReturn(
            "BfEscalationResult", "SendToDraft").deploy(rule);

        whenAtCallActivity("BF2_DRAFT").thenReturn("BfDraftResult", "QA").deploy(rule);

        whenAtCallActivity("BF2_QA").thenReturn("BfQaResult", "Accept").deploy(rule);

        whenAtCallActivity("BF2_SEND").deploy(rule);

        Scenario.run(processScenario).startByKey("BF2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_BF2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_REGISTRATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_BF2_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_DRAFT");
        verify(processScenario, times(2)).hasCompleted("CallActivity_BF2_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_QA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_BF2");
    }

    @Test
    public void testQA() {
        whenAtCallActivity("BF2_REGISTRATION").deploy(rule);

        whenAtCallActivity("BF2_TRIAGE").thenReturn("BFTriageResult", "Draft", "BfTriageAccept", "Yes").deploy(rule);

        whenAtCallActivity("BF2_QA").thenReturn("BfQaResult", "Accept").deploy(rule);

        whenAtCallActivity("BF2_DRAFT").thenReturn("BfDraftResult", "QA").deploy(rule);

        whenAtCallActivity("BF2_SEND").deploy(rule);

        Scenario.run(processScenario).startByKey("BF2").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_BF2");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_QA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_BF2_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_BF2");
    }

    @Test
    public void testTriageComplete() {
        whenAtCallActivity("BF2_REGISTRATION").deploy(rule);

        whenAtCallActivity("BF2_TRIAGE").thenReturn("BFTriageResult", "Complete", "BfTriageAccept", "Yes").deploy(rule);

        Scenario.run(processScenario).startByKey("BF2").execute();

        verify(processScenario).hasCompleted("StartEvent_BF2");
        verify(processScenario).hasCompleted("CallActivity_BF2_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_BF2_TRIAGE");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_BF2");
    }

    @Test
    public void testOfflineTransfer() {
        whenAtCallActivity("BF2_REGISTRATION").deploy(rule);

        whenAtCallActivity("BF2_TRIAGE").thenReturn("BfTriageAccept", "No").deploy(rule);

        Scenario.run(processScenario).startByKey("BF2").execute();

        verify(processScenario).hasCompleted("StartEvent_BF2");
        verify(processScenario).hasCompleted("CallActivity_BF2_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_BF2_TRIAGE");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_BF2");
    }

}
