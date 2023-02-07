package uk.gov.digital.ho.hocs.workflow.processes.comp;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/COMP/COMP_EXGRATIA.bpmn", "processes/COMP/COMP_EXGRATIA_RESPONSE_DRAFT.bpmn",
    "processes/COMP/COMP_EXGRATIA_ESCALATE.bpmn", "processes/COMP/COMP_EXGRATIA_QA.bpmn", "processes/COMP/COMP_EXGRATIA_SEND.bpmn",
    "processes/COMP/COMP_EXGRATIA_TRIAGE.bpmn", "processes/STAGE.bpmn", "processes/STAGE_WITH_USER.bpmn" })
public class COMP_EXGRATIA {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.9).build();

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
    public void testWhenExGratia() {

        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_RESPONSE_DRAFT").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(bpmnService).updateDeadlineDays(any(), any(), eq("60"));
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @Test
    public void testWhenExGratia_ReturnToCch() {

        whenAtCallActivity("COMP_EXGRATIA_TRIAGE")
            .thenReturn("CctTriageAccept", "No", "CctCompType", "CCH")
            .deploy(rule);


        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @Test
    public void testWhenExGratia_HardCloseAfterTriage() {

        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageResult", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @Test
    public void testWhenExGratia_TriageEscalate() {

        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Escalate").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_ESCALATE").thenReturn("CctEscalateResult", "Triage").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_RESPONSE_DRAFT").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @Test
    public void testWhenExGratia_DraftEscalate() {
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_RESPONSE_DRAFT").thenReturn("CctDraftResult", "Escalate").thenReturn("CctDraftResult",
            "Send").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_ESCALATE").thenReturn("CctEscalateResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_EXGRATIA_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @Test
    public void testWhenExGratia_WhenQaRejectThenAccept() {
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_RESPONSE_DRAFT").thenReturn("CctDraftResult", "QA").thenReturn("CctDraftResult",
            "QA").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_QA").thenReturn("CctQaResult", "Reject").thenReturn("CctQaResult",
            "Accept").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_EXGRATIA_RESPONSE_DRAFT");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_EXGRATIA_QA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromTriageResult() {
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromEscalate() {
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Escalate").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_ESCALATE").thenReturn("CctEscalateResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @Test
    public void testCompTypeExGratia_EscalateToPsuFromQa() {
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_RESPONSE_DRAFT").thenReturn("CctDraftResult", "QA").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_QA").thenReturn("CctQaResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_EXGRATIA").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_EXGRATIA");
        verify(processScenario, times(1)).hasCompleted("Service_UpdateDeadline");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_EXGRATIA_QA");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_EXGRATIA");
    }

    @After
    public void after() {
        RepositoryService repositoryService = processEngineRule.getRepositoryService();

        List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId());
        }

        Mocks.reset();
    }

}
