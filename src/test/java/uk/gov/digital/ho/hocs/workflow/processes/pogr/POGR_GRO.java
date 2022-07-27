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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/POGR/POGR_GRO.bpmn",
        "processes/STAGE_WITH_USER.bpmn" })
public class POGR_GRO {

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
        whenAtCallActivity("POGR_GRO_TRIAGE")
                .thenReturn("InvestigationOutcome", "", "CloseCaseTriage", "false")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "", "CloseCaseDraft", "false")
                .thenReturn("DraftOutcome", "QA", "CloseCaseDraft", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroTriage");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_Gro");
    }

    @Test
    public void testTriageCloseCase() {
        whenAtCallActivity("POGR_GRO_TRIAGE")
                .thenReturn("CloseCaseTriage", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario).hasCompleted("CallActivity_PogrGroTriage");
        verify(processScenario).hasCompleted("EndEvent_GroTriageEnd");
    }

    @Test
    public void testDraftReturnTriage() {
        whenAtCallActivity("POGR_GRO_TRIAGE")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "ReturnInvestigation", "CloseCaseDraft", "false")
                .thenReturn("DraftOutcome", "QA")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroTriage");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_Gro");
    }

    @Test
    public void testDraftCloseCase() {
        whenAtCallActivity("POGR_GRO_TRIAGE")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("CloseCaseDraft", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario).hasCompleted("CallActivity_PogrGroTriage");
        verify(processScenario).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_GroDraftEnd");
    }

    @Test
    public void testQaReject() {
        whenAtCallActivity("POGR_GRO_TRIAGE")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "QA")
                .thenReturn("DraftOutcome", "Dispatch")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_QA")
                .thenReturn("QaOutcome", "Reject", "reallocate", "true")
                .deploy(rule);


        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario).hasCompleted("CallActivity_PogrGroTriage");
        verify(processScenario).hasCompleted("CallActivity_PogrGroQa");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroDraft");
    }

    @Test
    public void testDraftOutComeCloseCase() {
        whenAtCallActivity("POGR_GRO_TRIAGE")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseTriage", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "CloseCase")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario).hasCompleted("CallActivity_PogrGroTriage");
        verify(processScenario).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_GroDraftEnd");
    }
}
