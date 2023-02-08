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

import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/COMP/COMP_SERVICE.bpmn", "processes/COMP/COMP_SERVICE_DRAFT.bpmn",
    "processes/COMP/COMP_SERVICE_ESCALATE.bpmn", "processes/COMP/COMP_SERVICE_QA.bpmn", "processes/COMP/COMP_SERVICE_SEND.bpmn",
    "processes/COMP/COMP_SERVICE_TRIAGE.bpmn", "processes/STAGE.bpmn", "processes/STAGE_WITH_USER.bpmn" })
public class COMP_SERVICE {

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
    public void testWhenService() {

        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
    }

    @Test
    public void testWhenService_ReturnToCch() {

        whenAtCallActivity("COMP_SERVICE_TRIAGE")
            .thenReturn("CctTriageAccept", "No", "CctCompType", "CCH")
            .deploy(rule);


        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
    }

    @Test
    public void testWhenService_HardCloseAfterTriage() {

        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageResult", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
    }

    @Test
    public void testWhenService_TriageEscalate() {

        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Escalate").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_ESCALATE").thenReturn("CctEscalateResult", "Triage").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
    }

    @Test
    public void testWhenService_DraftEscalate() {
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "Escalate").thenReturn("CctDraftResult",
            "Send").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_ESCALATE").thenReturn("CctEscalateResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
    }

    @Test
    public void testWhenService_WhenQaRejectThenAccept() {
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "QA").thenReturn("CctDraftResult",
            "QA").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_QA").thenReturn("CctQaResult", "Reject").thenReturn("CctQaResult",
            "Accept").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_SERVICE_QA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
    }

    @Test
    public void testCompTypeService_EscalateToPsuFromTriageResult() {
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
    }

    @Test
    public void testCompTypeService_EscalateToPsuFromEscalate() {
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Escalate").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_ESCALATE").thenReturn("CctEscalateResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
    }

    @Test
    public void testCompTypeService_EscalateToPsuFromQa() {
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "QA").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_QA").thenReturn("CctQaResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_SERVICE").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_SERVICE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_QA");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_SERVICE");
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
