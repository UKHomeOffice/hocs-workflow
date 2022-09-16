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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/POGR/POGR_HMPO_DRAFT.bpmn" })
public class POGR_HMPO_DRAFT {

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
        when(processScenario.waitsAtUserTask("Screen_DraftInput")).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", ""))).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "QA")));

        Scenario.run(processScenario).startByKey("POGR_HMPO_DRAFT").execute();

        verify(processScenario).hasCompleted("StartEvent_HmpoDraft");
        verify(processScenario, times(2)).hasCompleted("Screen_DraftInput");
        verify(processScenario).hasCompleted("Service_ClearRejectedValue");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasCompleted("EndEvent_HmpoDraft");
    }

    @Test
    public void testTelephoneResponse() {
        when(processScenario.waitsAtUserTask("Screen_DraftInput")).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "TelephoneResponse")));

        whenAtCallActivity("POGR_TELEPHONE_RESPONSE").thenReturn("TelephoneResponse", "", "DIRECTION",
            "Backward").thenReturn("TelephoneResponse", "").thenReturn("TelephoneResponse", "Yes").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR_HMPO_DRAFT").execute();

        verify(processScenario).hasCompleted("StartEvent_HmpoDraft");
        verify(processScenario).hasCompleted("Screen_DraftInput");
        verify(processScenario, times(3)).hasCompleted("CallActivity_TelephoneResponse");
        verify(processScenario).hasCompleted("EndEvent_HmpoDraft");
    }

    @Test
    public void testNotTelephoneResponse() {
        when(processScenario.waitsAtUserTask("Screen_DraftInput")).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "TelephoneResponse"))).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "TelephoneResponse"))).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "QA")));

        whenAtCallActivity("POGR_TELEPHONE_RESPONSE").thenReturn("DIRECTION", "BACKWARD").thenReturn(
            "TelephoneResponse", "No").deploy(rule);

        Scenario.run(processScenario).startByKey("POGR_HMPO_DRAFT").execute();

        verify(processScenario).hasCompleted("StartEvent_HmpoDraft");
        verify(processScenario, times(3)).hasCompleted("Screen_DraftInput");
        verify(processScenario, times(2)).hasCompleted("CallActivity_TelephoneResponse");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasCompleted("EndEvent_HmpoDraft");
    }

    @Test
    public void testRejectionPath() {
        when(processScenario.waitsAtUserTask("Screen_DraftInput")).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "ReturnInvestigation"))).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "ReturnInvestigation")));

        when(processScenario.waitsAtUserTask("Screen_RejectDraft")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "UNKNOWN"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startByKey("POGR_HMPO_DRAFT").execute();

        verify(processScenario).hasCompleted("StartEvent_HmpoDraft");
        verify(processScenario, times(2)).hasCompleted("Screen_DraftInput");
        verify(processScenario, times(3)).hasCompleted("Screen_RejectDraft");
        verify(processScenario).hasCompleted("Service_RejectCaseNote");
        verify(processScenario).hasCompleted("Service_ClearRejectedValue");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasCompleted("EndEvent_HmpoDraft");
    }

    @Test
    public void testEscalationPath() {
        when(processScenario.waitsAtUserTask("Screen_DraftInput")).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "Escalate"))).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "Escalate")));

        when(processScenario.waitsAtUserTask("Screen_HmpoEscalateScreen")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "UNKNOWN"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startByKey("POGR_HMPO_DRAFT").execute();

        verify(processScenario).hasCompleted("StartEvent_HmpoDraft");
        verify(processScenario, times(2)).hasCompleted("Screen_DraftInput");
        verify(processScenario, times(1)).hasCompleted("Service_EscalateCaseNote");
        verify(processScenario).hasCompleted("EndEvent_HmpoDraft");
    }

    @Test
    public void testCloseCaseAtDraftStage() {
        when(processScenario.waitsAtUserTask("Screen_DraftInput")).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "CloseCase"))).thenReturn(
            task -> task.complete(withVariables("DraftOutcome", "CloseCase")));

        whenAtCallActivity("POGR_CLOSE_CASE").thenReturn("CloseCase", Boolean.toString(false)).thenReturn("CloseCase",
            Boolean.toString(true)).deploy(rule);

        Scenario.run(processScenario).startByKey("POGR_HMPO_DRAFT").execute();

        verify(processScenario).hasCompleted("StartEvent_HmpoDraft");
        verify(processScenario, times(2)).hasCompleted("Screen_DraftInput");
        verify(processScenario, times(2)).hasCompleted("CallActivity_DraftCloseCase");
        verify(processScenario).hasCompleted("EndEvent_HmpoDraft");
    }

}
