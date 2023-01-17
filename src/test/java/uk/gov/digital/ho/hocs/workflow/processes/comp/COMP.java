package uk.gov.digital.ho.hocs.workflow.processes.comp;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/COMP/COMP.bpmn", "processes/COMP/COMP_CCH_REOPENED.bpmn",
    "processes/COMP/COMP_CCH_RETURNS.bpmn", "processes/COMP/COMP_CLOSED.bpmn", "processes/COMP/COMP_MINOR_CHECK.bpmn",
    "processes/COMP/COMP_MINOR_RESP.bpmn", "processes/COMP/COMP_OTHER.bpmn", "processes/COMP/COMP_REGISTRATION.bpmn",
    "processes/COMP/COMP_SERVICE_DRAFT.bpmn", "processes/COMP/COMP_SERVICE_ESCALATE.bpmn",
    "processes/COMP/COMP_SERVICE_QA.bpmn", "processes/COMP/COMP_SERVICE_SEND.bpmn",
    "processes/COMP/COMP_SERVICE_TRIAGE.bpmn", "processes/STAGE.bpmn", "processes/STAGE_WITH_USER.bpmn" })
public class COMP {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.30).build();

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

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenService_ReturnToCch() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "No", "CctCompType", "CCH").deploy(
            rule);
        whenAtCallActivity("COMP_CCH_RETURNS").thenReturn("CchCompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_RETURNS");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenService_TriageEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Escalate").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_ESCALATE").thenReturn("CctEscalateResult", "Triage").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenService_DraftEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "Escalate").thenReturn("CctDraftResult",
            "Send").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_ESCALATE").thenReturn("CctEscalateResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenService_WhenQaRejectThenAccept() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_DRAFT").thenReturn("CctDraftResult", "QA").thenReturn("CctDraftResult",
            "QA").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_QA").thenReturn("CctQaResult", "Reject").thenReturn("CctQaResult",
            "Accept").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_SERVICE_DRAFT");
        verify(processScenario, times(2)).hasCompleted("CallActivity_COMP_SERVICE_QA");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenMinor() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Minor", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_MINOR_CHECK").thenReturn("MinorAccept", "Yes").deploy(rule);
        whenAtCallActivity("COMP_MINOR_RESP").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINOR_CHECK");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_MINOR_RESP");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenExGratia() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_RESPONSE_DRAFT").thenReturn("CctDraftResult", "default").thenReturn(
            "CctDraftResult", "QA").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_QA").thenReturn("CctQaResult", "Reject").thenReturn("CctQaResult",
            "Accept").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario).hasCompleted("Service_UpdateDeadline");
        verify(bpmnService).updateDeadlineDays(any(), any(), eq("60"));
        verify(processScenario, times(1)).hasCompleted("CallActivity_EXGRATIA_TRIAGE");
        verify(processScenario, times(3)).hasCompleted("CallActivity_EX_GRATIA_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_EX_GRATIA_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenExGratiaEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Escalate").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_ESCALATE").thenReturn("CctEscalateResult", "Triage").thenReturn(
            "CctEscalateResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_RESPONSE_DRAFT").thenReturn("CctDraftResult", "Escalate").thenReturn(
            "CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_SEND").thenReturn("CctDraftResult", "Send").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_EXGRATIA_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_EX_GRATIA_RESPONSE_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_EX_GRATIA_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenExGratia_ReturnToCch() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia", "Stage", "Stage1").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "No", "CctCompType", "CCH").deploy(
            rule);
        whenAtCallActivity("COMP_CCH_RETURNS").thenReturn("CchCompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_RETURNS");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenMinorMisconduct() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct", "Stage", "Stage1").deploy(
            rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").thenReturn("CctDraftResult", "default").thenReturn(
            "CctDraftResult", "QA").thenReturn("CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_QA").thenReturn("CctQaResult", "Reject").thenReturn("CctQaResult",
            "Accept").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_SEND").thenReturn("CctDraftResult", "Send", "Stage", "Stage1").deploy(
            rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_TRIAGE");
        verify(processScenario, times(3)).hasCompleted("CallActivity_MM_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenMinorMisconductEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct", "Stage", "Stage1").deploy(
            rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult",
            "Escalate").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_ESCALATE").thenReturn("CctEscalateResult", "Triage").thenReturn(
            "CctEscalateResult", "Draft").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").thenReturn("CctDraftResult", "Escalate").thenReturn(
            "CctDraftResult", "Send").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_SEND").thenReturn("CctDraftResult", "Send").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_MM_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_MM_DRAFT");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_SEND");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenMinorMisconduct_ReturnToCch() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct", "Stage", "Stage1").deploy(
            rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "No", "CctCompType",
            "CCH").deploy(rule);
        whenAtCallActivity("COMP_CCH_RETURNS").thenReturn("CchCompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_RETURNS");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenMinorMisconduct_HardCloseAfterTriage() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct", "Stage", "Stage1").deploy(
            rule);

        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageResult", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testWhenExGratia_HardCloseAfterTriage() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia", "Stage", "Stage1").deploy(rule);

        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageResult", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeServiceReturnToCch() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "CCH", "CctCompType", "CCH").deploy(rule);
        whenAtCallActivity("COMP_CCH_RETURNS").thenReturn("CchCompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_RETURNS");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeServiceReturnToCchTriageAcceptOldValue() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "No", "CctCompType", "CCH").deploy(rule);
        whenAtCallActivity("COMP_CCH_RETURNS").thenReturn("CchCompType", "Complete").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_RETURNS");
        verify(processScenario, times(1)).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeServiceEscalateToPsu() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeServiceEscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeServiceEscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Service").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Escalate").deploy(rule);
        whenAtCallActivity("COMP_SERVICE_ESCALATE").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_SERVICE_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeMinorMisconductEscalateToPsu() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeMinorMisconductEscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeMinorMisconductEscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "MinorMisconduct").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Escalate").deploy(rule);
        whenAtCallActivity("COMP_MINORMISCONDUCT_ESCALATE").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_MM_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeExGratiaEscalateToPsu() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeExGratiaEscalateToPsuFromTriageResult() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void testCompTypeExGratiaEscalateToPsuFromEscalate() {

        whenAtCallActivity("COMP_REGISTRATION").thenReturn("CompType", "Ex-Gratia").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_TRIAGE").thenReturn("CctTriageAccept", "Yes", "CctTriageResult", "Escalate").deploy(rule);
        whenAtCallActivity("COMP_EXGRATIA_ESCALATE").thenReturn("CctEscalateResult", "PSU").deploy(rule);
        whenAtCallActivity("PSU_COMP_COMPLAINT").thenReturn("ReturnCase", "false").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_COMP");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario, times(1)).hasCompleted("CallActivity_EXGRATIA_TRIAGE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_EX_GRATIA_ESCALATE");
        verify(processScenario, times(1)).hasCompleted("CallActivity_COMP_PSU");
        verify(processScenario, times(1)).hasCompleted("EndEvent_COMP");
    }

    @Test
    public void whenWebformClosureCompletesCase() {
        whenAtCallActivity("COMP_REGISTRATION").thenReturn("WebformComplaintInvalid", "Yes").deploy(rule);

        Scenario.run(processScenario).startByKey("COMP", Map.of("Channel", "Webform")).execute();

        verify(processScenario).hasCompleted("StartEvent_COMP");
        verify(processScenario).hasCompleted("CallActivity_COMP_REGISTRATION");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_COMP");
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
