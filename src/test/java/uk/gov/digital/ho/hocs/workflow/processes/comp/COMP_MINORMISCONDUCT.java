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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/COMP/COMP_MINORMISCONDUCT.bpmn", "processes/COMP/COMP_MINORMISCONDUCT_RESPONSE_DRAFT.bpmn",
    "processes/COMP/COMP_MINORMISCONDUCT_ESCALATE.bpmn", "processes/COMP/COMP_MINORMISCONDUCT_QA.bpmn",
    "processes/COMP/COMP_MINORMISCONDUCT_SEND.bpmn", "processes/COMP/COMP_MINORMISCONDUCT_TRIAGE.bpmn",
    "processes/STAGE.bpmn", "processes/STAGE_WITH_USER.bpmn" })
public class COMP_MINORMISCONDUCT {

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
    public void testWhenMinorMisconduct() {

        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
    }

    @Test
    public void testWhenMinorMisconduct_ReturnToCch() {

        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE")
            .thenReturn("CctTriageAccept", "No", "CctCompType", "CCH")
            .deploy(rule);


        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
    }

    @Test
    public void testWhenMinorMisconduct_HardCloseAfterTriage() {

        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageResult", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
    }

    @Test
    public void testWhenMinorMisconduct_TriageEscalate() {

        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Escalate").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_ESCALATE").thenReturn("CctEscalateResult", "Triage").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
    }

    @Test
    public void testWhenMinorMisconduct_DraftEscalate() {
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").thenReturn("CctDraftResult", "Escalate").thenReturn("CctDraftResult",
            "Send").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_ESCALATE").thenReturn("CctEscalateResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
    }

    @Test
    public void testWhenMinorMisconduct_WhenQaRejectThenAccept() {
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").thenReturn("CctDraftResult", "QA").thenReturn("CctDraftResult",
            "QA").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_QA").thenReturn("CctQaResult", "Reject").thenReturn("CctQaResult",
            "Accept").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_RESPONSE_DRAFT");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_QA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsuFromTriageResult() {
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsuFromEscalate() {
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Escalate").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_ESCALATE").thenReturn("CctEscalateResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
    }

    @Test
    public void testCompTypeMinorMisconduct_EscalateToPsuFromQa() {
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").thenReturn("CctDraftResult", "QA").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_QA").thenReturn("CctQaResult", "PSU").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP_MINORMISCONDUCT").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP_MINORMISCONDUCT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINORMISCONDUCT_QA");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP_MINORMISCONDUCT");
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
