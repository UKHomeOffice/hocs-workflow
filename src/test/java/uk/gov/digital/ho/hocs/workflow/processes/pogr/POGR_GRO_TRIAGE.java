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
        "processes/POGR/POGR_GRO_TRIAGE.bpmn"
})
public class POGR_GRO_TRIAGE {

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
        when(processScenario.waitsAtUserTask("Screen_TriageAcceptCase"))
                .thenReturn(task -> task.complete(withVariables("TriageAccept", "")))
                .thenReturn(task -> task.complete(withVariables("TriageAccept", "Accept")));

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("InvestigationOutcome", "Pending")
                .thenReturn("InvestigationOutcome", "Draft")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("Screen_TriageAcceptCase");
        verify(processScenario, times(2)).hasCompleted("CallActivity_TriageInvestigation");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO_TRIAGE");
    }

    @Test
    public void testTriageExternalCloseCase() {
        when(processScenario.waitsAtUserTask("Screen_TriageAcceptCase"))
                .thenReturn(task -> task.complete(withVariables("TriageAccept", "Reject")));

        when(processScenario.waitsAtUserTask("Screen_GroTransferScreen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "TriageAccept", "Reject", "TriageRejectOptions", "External")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("Screen_TriageAcceptCase");
        verify(processScenario, times(2)).hasCompleted("Screen_GroTransferScreen");
        verify(processScenario).hasCompleted("Activity_CloseCaseNote");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO_TRIAGE");

        verify(bpmnService).updateAllocationNote(any(), any(), any(), eq("REJECT"));
        verify(bpmnService, times(0)).updateTeamByStageAndTexts(any(), any(), eq("POGR_GRO_TRIAGE"), any(), any());
    }

    @Test
    public void testTriageInternalCloseCase() {
        when(processScenario.waitsAtUserTask("Screen_TriageAcceptCase"))
                .thenReturn(task -> task.complete(withVariables("TriageAccept", "Reject")));

        when(processScenario.waitsAtUserTask("Screen_GroTransferScreen"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "TriageRejectOptions", "Internal")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("Screen_TriageAcceptCase");
        verify(processScenario, times(2)).hasCompleted("Screen_GroTransferScreen");
        verify(processScenario).hasCompleted("Activity_CloseCaseNote");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO_TRIAGE");

        verify(bpmnService).updateAllocationNote(any(), any(), any(), eq("REJECT"));
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("POGR_GRO_TRIAGE"), any(), any(), any());

    }

}
