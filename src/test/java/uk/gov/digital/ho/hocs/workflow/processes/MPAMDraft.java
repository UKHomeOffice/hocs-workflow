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
@Deployment(resources = "processes/MPAM_DRAFT.bpmn")
public class MPAMDraft {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.1)
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
    public void whenDispatch_thenUpdateTeam_andClearRejected() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "DraftStatus", "Dispatch")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("Service_UpdateTeamForDispatch");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DISPATCH"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasCompleted("Service_ClearRejected");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasFinished("EndEvent_MpamDraft");
    }

    @Test
    public void whenOfficialChangedToMinisterial_thenMinisterialValuesAreNotCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Official",
                        "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial_0zaqr6s"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("Screen_ReferenceTypeToMinisterial_1unkg32");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToMinisterial_0urk37h");
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote_1yc1ce2");
        verify(processScenario).hasFinished("EndEvent_MpamDraft");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Ministerial"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService, never()).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
    }

    @Test
    public void whenMinisterialChangedToOfficial_thenMinisterialValuesAreCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateRefType",
                        "RefType", "Ministerial",
                        "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_RefTypeToOfficial_08l10xy"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("Screen_RefTypeToOfficial_0ia1i5c");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToOfficial_0lic0m6");
        verify(processScenario).hasCompleted("Service_ClearMinisterialValues_0xqy6p5");
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote_1yc1ce2");
        verify(processScenario).hasFinished("EndEvent_MpamDraft");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Official"), eq("RefTypeStatus"), eq("Confirm"));
        verify(bpmnService).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
    }

    @Test
    public void whenPutOnCampaign_thenUpdateTeam_andClearRejected() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "DraftStatus", "PutOnCampaign")));

        when(processScenario.waitsAtUserTask("Validate_RequestCampaign"))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", true)));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT")
                .execute();

        verify(processScenario, times(2)).hasCompleted("Service_ClearCampaignType");
        verify(bpmnService, times(2)).blankCaseValues(any(), any(), eq("CampaignType"));
        verify(processScenario, times(3)).hasCompleted("Screen_RequestCampaign");
        verify(processScenario).hasCompleted("Service_UpdateTeamForCampaign");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_CAMPAIGN"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasCompleted("Service_ClearRejected");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasFinished("EndEvent_MpamDraft");
    }

    @Test
    public void whenReturnToTriage_thenCasenoteCreated_andMinisterialValuesAreCleared_andUpdatesTeam() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "DraftStatus", "ReturnToTriage")));
        when(processScenario.waitsAtUserTask("Validate_ReturnToTriageInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_DraftReturnToTriage", "Case note")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("Screen_ReturnToTriageInput");
        verify(processScenario).hasCompleted("Service_SaveReturnReasonNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Case note"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByDraft");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By Draft"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForTriage");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRIAGE"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamDraft");
    }

    @Test
    public void whenQa_thenUpdateTeam_andClearRejected() {

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "DraftStatus", "QA")));

        Scenario.run(processScenario)
                .startByKey("MPAM_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("Service_UpdateTeamForQa");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_QA"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasCompleted("Service_ClearRejected");
        verify(bpmnService).blankCaseValues(any(), any(), eq("Rejected"));
        verify(processScenario).hasFinished("EndEvent_MpamDraft");
    }
}
