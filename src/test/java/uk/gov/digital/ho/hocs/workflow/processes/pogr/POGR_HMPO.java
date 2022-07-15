package uk.gov.digital.ho.hocs.workflow.processes.pogr;

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
@Deployment(resources = {
        "processes/POGR/POGR_HMPO.bpmn",
        "processes/STAGE_WITH_USER.bpmn" })
public class POGR_HMPO {

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
    public void testHappyPath() {
        whenAtCallActivity("POGR_HMPO_TRIAGE")
                .thenReturn("InvestigationOutcome", "", "CloseCaseTriage", "false")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_DRAFT")
                .thenReturn("DraftOutcome", "", "CloseCaseDraft", "false")
                .thenReturn("DraftOutcome", "QA", "CloseCaseDraft", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_DISPATCH")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_HMPO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoTriage");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("EndEvent_Hmpo");
    }

    @Test
    public void testTriageCloseCase() {
        whenAtCallActivity("POGR_HMPO_TRIAGE")
                .thenReturn("CloseCaseTriage", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_HMPO")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoTriage");
        verify(processScenario).hasCompleted("EndEvent_HmpoTriageEnd");
    }

    @Test
    public void testDraftReturnTriage() {
        whenAtCallActivity("POGR_HMPO_TRIAGE")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_DRAFT")
                .thenReturn("DraftOutcome", "ReturnInvestigation", "CloseCaseDraft", "false")
                .thenReturn("DraftOutcome", "QA", "CloseCaseDraft", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_HMPO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoTriage");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("EndEvent_Hmpo");
    }

    @Test
    public void testDraftCloseCase() {
        whenAtCallActivity("POGR_HMPO_TRIAGE")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_DRAFT")
                .thenReturn("CloseCaseDraft", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_HMPO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoTriage");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("EndEvent_HmpoDraftEnd");
    }

    @Test
    public void testQaReject() {
        whenAtCallActivity("POGR_HMPO_TRIAGE")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_DRAFT")
                .thenReturn("DraftOutcome", "QA")
                .thenReturn("DraftOutcome", "Dispatch")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_QA")
                .thenReturn("QaOutcome", "Reject", "reallocate", "true")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_DISPATCH")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_HMPO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoTriage");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoQa");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrHmpoDraft");
    }

    @Test
    public void testQaBypassStraightToDispatch() {
        whenAtCallActivity("POGR_HMPO_TRIAGE")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_DRAFT")
                .thenReturn("DraftOutcome", "QA")
                .thenReturn("DraftOutcome", "Dispatch")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        whenAtCallActivity("POGR_HMPO_DISPATCH")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_HMPO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Hmpo");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrHmpoTriage");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrHmpoQa");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoDraft");
        verify(processScenario).hasCompleted("CallActivity_PogrHmpoDispatch");
    }

}
