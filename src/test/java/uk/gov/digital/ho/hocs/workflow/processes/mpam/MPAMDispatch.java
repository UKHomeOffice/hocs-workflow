package uk.gov.digital.ho.hocs.workflow.processes.mpam;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM/MPAM_DISPATCH.bpmn")
public class MPAMDispatch {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.2).build();

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
    public void whenReturnToDraft_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam() {

        when(processScenario.waitsAtUserTask("Validate_UserInput")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "DispatchStatus", "ReturnToDraft")));
        when(processScenario.waitsAtUserTask("Validate_ReturnToDraftInput")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_DispatchReturnToDraft", "Casenote Reject")));

        Scenario.run(processScenario).startByKey("MPAM_DISPATCH").execute();

        verify(processScenario).hasCompleted("Screen_ReturnToDraftInput");
        verify(processScenario).hasCompleted("Service_SaveReturnReasonNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByDispatch");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By Dispatch"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForDraft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), eq("QueueTeamUUID"),
            eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamDispatch");
    }

}
