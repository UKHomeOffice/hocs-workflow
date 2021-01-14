package uk.gov.digital.ho.hocs.workflow.processes;

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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM_QA.bpmn")
public class MPAMQa {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.2)
            .build();

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
    public void whenRejectDraft_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "Reject-Draft")));
        when(processScenario.waitsAtUserTask("Validate_RejectToDraft"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectDraft", "Casenote Reject")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(processScenario).hasCompleted("Screen_RejectToDraft");
        verify(processScenario).hasCompleted("Service_SaveRejectDraftNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByQa_RejectToDraft");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By QA"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForDraft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamQa");
    }

    @Test
    public void whenRejectTriage_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "Reject-Triage")));
        when(processScenario.waitsAtUserTask("Validate_RejectToTriage"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectTriage", "Casenote Reject")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(processScenario).hasCompleted("Screen_RejectToTriage");
        verify(processScenario).hasCompleted("Service_SaveRejectTriageNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByQa_RejectToTriage");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By QA"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForTriage");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRIAGE"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamQa");
    }
}
