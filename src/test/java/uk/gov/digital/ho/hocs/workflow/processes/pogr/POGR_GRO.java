package uk.gov.digital.ho.hocs.workflow.processes.pogr;

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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/POGR/POGR_GRO.bpmn",
        "processes/STAGE_WITH_USER.bpmn",
        "processes/STAGE.bpmn" })
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
        whenAtCallActivity("POGR_GRO_INVESTIGATION")
                .thenReturn("InvestigationOutcome", "", "CloseCaseInvestigation", "false")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "", "CloseCaseDraft", "false")
                .thenReturn("DraftOutcome", "QA", "CloseCaseDraft", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DISPATCH")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroInvestigation");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_Gro");
    }

    @Test
    public void testInvestigationCloseCase() {
        whenAtCallActivity("POGR_GRO_INVESTIGATION")
                .thenReturn("CloseCase", Boolean.toString(true))
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario).hasCompleted("CallActivity_PogrGroInvestigation");
        verify(processScenario).hasCompleted("EndEvent_GroInvestigationEnd");
    }

    @Test
    public void testDraftReturnInvestigation() {
        whenAtCallActivity("POGR_GRO_INVESTIGATION")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false")
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
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroInvestigation");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_Gro");
    }

    @Test
    public void testDraftCloseCase() {
        whenAtCallActivity("POGR_GRO_INVESTIGATION")
                .thenReturn("InvestigationOutcome", "Draft")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("CloseCase", Boolean.toString(true))
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario).hasCompleted("CallActivity_PogrGroInvestigation");
        verify(processScenario).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_GroDraftEnd");
    }

    @Test
    public void testQaReject() {
        whenAtCallActivity("POGR_GRO_INVESTIGATION")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "QA")
                .thenReturn("DraftOutcome", "Dispatch")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_QA")
                .thenReturn("QaOutcome", "Reject", "reallocate", "true")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DISPATCH")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario).hasCompleted("CallActivity_PogrGroInvestigation");
        verify(processScenario).hasCompleted("CallActivity_PogrGroQa");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroDraft");
    }

    @Test
    public void testQaBypassStraightToDispatch() {
        whenAtCallActivity("POGR_GRO_INVESTIGATION")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "Dispatch")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DISPATCH")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrGroInvestigation");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrGroDispatch");
        verify(processScenario).hasCompleted("EndEvent_Gro");
    }

    @Test
    public void testInvestigationEscalate() {
        whenAtCallActivity("POGR_GRO_INVESTIGATION")
                .thenReturn("InvestigationOutcome", "Escalate")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_ESCALATE")
                .thenReturn("EscalationOutcome", "Investigation")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "QA", "CloseCaseDraft", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DISPATCH")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroInvestigation");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrGroEscalate");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_Gro");
    }

    @Test
    public void testDraftEscalate() {
        whenAtCallActivity("POGR_GRO_INVESTIGATION")
                .thenReturn("InvestigationOutcome", "Draft", "CloseCaseInvestigation", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_ESCALATE")
                .thenReturn("EscalationOutcome", "Draft")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DRAFT")
                .thenReturn("DraftOutcome", "Escalate")
                .thenReturn("DraftOutcome", "QA", "CloseCaseDraft", "false")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_QA")
                .thenReturn("QaOutcome", "Accept")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_DISPATCH")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO", withVariables("LastUpdatedByUserUUID", "userUUID"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Gro");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrGroInvestigation");
        verify(processScenario, times(1)).hasCompleted("CallActivity_PogrGroEscalate");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroDraft");
        verify(processScenario).hasCompleted("EndEvent_Gro");
    }

}
