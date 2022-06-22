package uk.gov.digital.ho.hocs.workflow.processes.mpam;

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
@Deployment(resources = "processes/MPAM/MPAM_QA.bpmn")
public class MPAMQa {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(1)
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
        Mocks.register("BusArea", "UKVI");
        Mocks.register("RefType", "Official");
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
                        "DIRECTION", "BACKWARD",
                        "CaseNote_RejectDraft", "Casenote Reject")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectDraft", "Casenote Reject")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectDraft", "Casenote Reject")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(processScenario, times(3)).hasCompleted("Screen_RejectToDraft");
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
                        "valid", false,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "Reject-Triage")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "Reject-Triage")));
        when(processScenario.waitsAtUserTask("Validate_RejectToTriage"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "BACKWARD",
                        "CaseNote_RejectTriage", "Casenote Reject")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectTriage", "Casenote Reject")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectTriage", "Casenote Reject")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(processScenario, times(3)).hasCompleted("Screen_RejectToTriage");
        verify(processScenario).hasCompleted("Service_SaveRejectTriageNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByQa_RejectToTriage");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By QA"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForTriage");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRIAGE"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamQa");
    }

    @Test
    public void whenRequestSecretariatClearance_thenShowClearanceRequestScreen(){
        Mocks.register("RefType", "Ministerial");

        when(processScenario.waitsAtUserTask("Validate_UserInput_Clearance"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "RequestSecretariatClearance")));

        when(processScenario.waitsAtUserTask("validate_request_clearance_input"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(processScenario, times(1)).hasCompleted("validate_request_clearance_input");
        verify(processScenario, times(2)).hasCompleted("Validate_UserInput_Clearance");
    }

    @Test
    public void whenGoBackFromClearanceRequest_thenRestartDiagram(){
        Mocks.register("RefType", "Ministerial");

        when(processScenario.waitsAtUserTask("Validate_UserInput_Clearance"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "RequestSecretariatClearance")));

        when(processScenario.waitsAtUserTask("validate_request_clearance_input"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(processScenario, times(2)).hasCompleted("validate_request_clearance_input");
        verify(processScenario, times(2)).hasCompleted("Validate_UserInput_Clearance");
    }

    @Test
    public void whenPutOnCampaign_MoveToCampaignTeam(){
        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "PutOnCampaign")));

        when(processScenario.waitsAtUserTask("UserTask_1rbr6sp"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_CAMPAIGN"), any(), any(), any(), any());
    }

    @Test
    public void whenEscalate_UpdateAllocationNote(){
        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "Escalate")));

        when(processScenario.waitsAtUserTask("UserTask_1u19uk3"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(bpmnService).updateAllocationNote(any(), any(), any(), eq("SEND_TO_WORKFLOW_MANAGER"));
    }

    @Test
    public void whenCloseCase_CreateCaseNote(){
        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "Pending")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "CloseCaseTelephone")));

        when(processScenario.waitsAtUserTask("Activity_0oiigje"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(bpmnService).createCaseNote(any(), any(), eq("CLOSE_CASE_TELEPHONE"));
    }

    @Test
    public void whenApproveOfficial_UpdateTeamToDispatch(){
        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "Approve",
                        "RefType", "Official")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DISPATCH"), any(), any(), any(), any());
    }

    @Test
    public void whenApproveMinisterial_UpdateTeamToPO(){
        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "Approve",
                        "RefType", "Ministerial")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_PO"), any(), any(), any(), any());
    }

    @Test
    public void whenDefaultExit_LeavesQuietly(){
        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "QaStatus", "NoneOfTheAbove",
                        "RefType", "Ministerial")));

        Scenario.run(processScenario)
                .startByKey("MPAM_QA")
                .execute();

        verify(processScenario).hasCompleted("Validate_UserInput");
    }

}
