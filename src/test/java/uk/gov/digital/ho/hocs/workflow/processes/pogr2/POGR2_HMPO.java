package uk.gov.digital.ho.hocs.workflow.processes.pogr2;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/POGR2/POGR2_HMPO.bpmn", "processes/STAGE_WITH_USER.bpmn", "processes/STAGE.bpmn" })
public class POGR2_HMPO {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

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
        whenAtCallActivity("POGR2_HMPO_INVESTIGATION").thenReturn("InvestigationOutcome", "", "CloseCaseInvestigation",
            "false").thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DRAFT").thenReturn("DraftOutcome", "", "CloseCaseDraft", "false").thenReturn(
            "DraftOutcome", "QA", "CloseCaseDraft", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_QA").thenReturn("QaOutcome", "Accept").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DISPATCH").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR2_HMPO",
            withVariables("LastUpdatedByUserUUID", "userUUID")).execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoInvestigation");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("EndEvent_Hmpo");
    }

    @Test
    public void testInvestigationCloseCase() {
        whenAtCallActivity("POGR2_HMPO_INVESTIGATION").thenReturn("CloseCase", Boolean.toString(true)).deploy(rule);

        Scenario.run(processScenario).startByKey("POGR2_HMPO").execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoInvestigation");
        verify(processScenario).hasCompleted("EndEvent_HmpoInvestigationEnd");
    }

    @Test
    public void testDraftReturnInvestigation() {
        whenAtCallActivity("POGR2_HMPO_INVESTIGATION").thenReturn("InvestigationOutcome", "Draft",
            "CloseCaseInvestigation", "false").thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation",
            "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DRAFT").thenReturn("DraftOutcome", "ReturnInvestigation", "CloseCaseDraft",
            "false").thenReturn("DraftOutcome", "QA", "CloseCaseDraft", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_QA").thenReturn("QaOutcome", "Accept").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR2_HMPO",
            withVariables("LastUpdatedByUserUUID", "userUUID")).execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoInvestigation");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("EndEvent_Hmpo");
    }

    @Test
    public void testDraftCloseCase() {
        whenAtCallActivity("POGR2_HMPO_INVESTIGATION").thenReturn("InvestigationOutcome", "Draft").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DRAFT").thenReturn("CloseCase", Boolean.toString(true)).deploy(rule);

        Scenario.run(processScenario).startByKey("POGR2_HMPO",
            withVariables("LastUpdatedByUserUUID", "userUUID")).execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoInvestigation");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("EndEvent_HmpoDraftEnd");
    }

    @Test
    public void testQaReject() {
        whenAtCallActivity("POGR2_HMPO_INVESTIGATION").thenReturn("InvestigationOutcome", "Draft",
            "CloseCaseInvestigation", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DRAFT").thenReturn("DraftOutcome", "QA").thenReturn("DraftOutcome",
            "Dispatch").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_QA").thenReturn("QaOutcome", "").thenReturn("QaOutcome", "Reject").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DISPATCH").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR2_HMPO",
            withVariables("LastUpdatedByUserUUID", "userUUID")).execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoInvestigation");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoQa");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoDraft");
    }

    @Test
    public void testQaBypassStraightToDispatch() {
        whenAtCallActivity("POGR2_HMPO_INVESTIGATION").thenReturn("InvestigationOutcome", "Draft",
            "CloseCaseInvestigation", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DRAFT").thenReturn("DraftOutcome", "QA").thenReturn("DraftOutcome",
            "Dispatch").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_QA").thenReturn("QaOutcome", "Accept").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DISPATCH").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR2_HMPO",
            withVariables("LastUpdatedByUserUUID", "userUUID")).execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrHmpoInvestigation");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrHmpoQa");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoDispatch");
    }

    @Test
    public void testInvestigationEscalate() {
        whenAtCallActivity("POGR2_HMPO_INVESTIGATION").thenReturn("InvestigationOutcome", "Escalate").thenReturn(
            "InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_ESCALATE").thenReturn("EscalationOutcome", "").thenReturn("EscalationOutcome",
            "Investigation").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DRAFT").thenReturn("DraftOutcome", "QA", "CloseCaseDraft", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_QA").thenReturn("QaOutcome", "Accept").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DISPATCH").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR2_HMPO",
            withVariables("LastUpdatedByUserUUID", "userUUID")).execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoInvestigation");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoEscalate");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("EndEvent_Hmpo");
    }

    @Test
    public void testDraftEscalate() {
        whenAtCallActivity("POGR2_HMPO_INVESTIGATION").thenReturn("InvestigationOutcome", "Draft",
            "CloseCaseInvestigation", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_ESCALATE").thenReturn("EscalationOutcome", "Draft").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DRAFT").thenReturn("DraftOutcome", "Escalate").thenReturn("DraftOutcome", "QA",
            "CloseCaseDraft", "false").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_QA").thenReturn("QaOutcome", "Accept").deploy(rule);

        whenAtCallActivity("POGR2_HMPO_DISPATCH").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR2_HMPO",
            withVariables("LastUpdatedByUserUUID", "userUUID")).execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrHmpoInvestigation");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrHmpoEscalate");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("EndEvent_Hmpo");
    }

}
