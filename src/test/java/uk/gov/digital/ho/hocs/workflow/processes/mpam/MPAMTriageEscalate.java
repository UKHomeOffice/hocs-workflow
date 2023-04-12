package uk.gov.digital.ho.hocs.workflow.processes.mpam;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.mockito.ProcessExpressions;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/MPAM/MPAM_TRIAGE_ESCALATE.bpmn",
    "processes/MPAM/MPAM_UPDATE_ENQUIRY_SUBJECT_REASON.bpmn" })
public class MPAMTriageEscalate extends MPAMCommonTests {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.25).build();

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
    public void whenMinisterialChangedToOfficial_thenMinisterialValuesAreCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput")).thenReturn(task -> task.complete(
            withVariables("DIRECTION", "UpdateRefType", "RefType", "Ministerial", "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToOfficial")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(
                withVariables("DIRECTION", "FORWARD", "CaseNote_TriageChangeCaseType", "Casenote")));

        Scenario.run(processScenario).startByKey("MPAM_TRIAGE_ESCALATE").execute();

        verify(processScenario, times(2)).hasCompleted("Screen_ReferenceTypeToOfficial");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToOfficial");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Official"), eq("RefTypeStatus"),
            eq("Confirm"));
        verify(processScenario).hasCompleted("Service_ClearMinisterialValues");
        verify(bpmnService).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote");
        verify(bpmnService).createCaseConversionNote(any(), any(), eq("Casenote"));
        verify(processScenario).hasFinished("EndEvent_MpamTriageEscalate");
    }

    @Test
    public void whenRequestContributions_thenRequestContributions() {
        when(processScenario.waitsAtUserTask("Validate_UserInput")).thenReturn(task -> task.complete(
            withVariables("DIRECTION", "FORWARD", "TriageEscalateOutcome", "RequestContribution")));

        when(processScenario.waitsAtUserTask("UserTask_RequestContributionInput")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startByKey("MPAM_TRIAGE_ESCALATE").execute();

        verify(processScenario).hasCompleted("ServiceTask_RequestContributionInput");
        verify(processScenario).hasCompleted("UserTask_RequestContributionInput");
        verify(processScenario).hasFinished("EndEvent_MpamTriageEscalate");
    }

    @Test
    public void whenOfficialChangedToMinisterial_thenMinisterialValuesAreNotCleared() {

        when(processScenario.waitsAtUserTask("Validate_UserInput")).thenReturn(task -> task.complete(
            withVariables("DIRECTION", "UpdateRefType", "RefType", "Official", "RefTypeCorrection", "Correction")));
        when(processScenario.waitsAtUserTask("Validate_ReferenceTypeToMinisterial")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(
                withVariables("DIRECTION", "FORWARD", "CaseNote_TriageChangeCaseType", "Casenote")));

        Scenario.run(processScenario).startByKey("MPAM_TRIAGE_ESCALATE").execute();

        verify(processScenario, times(2)).hasCompleted("Screen_ReferenceTypeToMinisterial");
        verify(processScenario).hasCompleted("Service_UpdateRefTypeToMinisterial");
        verify(bpmnService).updateValue(any(), any(), eq("RefType"), eq("Ministerial"), eq("RefTypeStatus"),
            eq("Confirm"));
        verify(processScenario).hasCompleted("Service_SaveRefTypeChangeCaseNote");
        verify(bpmnService).createCaseConversionNote(any(), any(), eq("Casenote"));
        verify(processScenario).hasFinished("EndEvent_MpamTriageEscalate");
        verify(bpmnService, never()).blankCaseValues(any(), any(), eq("MinSignOffTeam"), eq("Addressee"));
    }

    @Test
    public void whenTransferToOGD_thenAddTransferNote_thenSetDueDate_thenUpdateTeamForTransfer() {

        when(processScenario.waitsAtUserTask("Validate_UserInput")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "UpdateBusinessArea")));

        when(processScenario.waitsAtUserTask("Validate_BusinessAreaChange")).thenReturn(
            task -> task.complete(withVariables( "DIRECTION", "FORWARD", "BusArea", "TransferToOgd")));

        Scenario.run(processScenario).startByKey("MPAM_TRIAGE_ESCALATE").execute();

        verify(processScenario).hasCompleted("Activity_0u4xxk6"); // create transfer note
        verify(processScenario).hasCompleted("Activity_0osk3xt"); // set transfer date
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRANSFER"), eq("QueueTeamUUID"),
            eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamTriageEscalate");
    }

    @Test
    public void whenTransferToOther_thenAddTransferNote_thenSetDueDate_thenUpdateTeamForTransfer() {

        when(processScenario.waitsAtUserTask("Validate_UserInput")).thenReturn(
            task -> task.complete(withVariables( "DIRECTION", "UpdateBusinessArea")));

        when(processScenario.waitsAtUserTask("Validate_BusinessAreaChange")).thenReturn(
            task -> task.complete(withVariables( "DIRECTION", "FORWARD", "BusArea", "TransferToOther")));

        Scenario.run(processScenario).startByKey("MPAM_TRIAGE_ESCALATE").execute();

        verify(processScenario).hasCompleted("Activity_0u4xxk6"); // create transfer note
        verify(processScenario).hasCompleted("Activity_0osk3xt"); // set transfer date
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRANSFER"), eq("QueueTeamUUID"),
            eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamTriageEscalate");
    }

    @Test
    public void whenNotTransferToOther_thenUpdateTeamForDraft() {

        when(processScenario.waitsAtUserTask("Validate_UserInput")).thenReturn(
            task -> task.complete(withVariables( "DIRECTION", "UpdateBusinessArea")));

        when(processScenario.waitsAtUserTask("Validate_BusinessAreaChange")).thenReturn(task -> task.complete(
            withVariables( "DIRECTION", "FORWARD", "BusArea", "NotTransferToOther")));

        Scenario.run(processScenario).startByKey("MPAM_TRIAGE_ESCALATE").execute();

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRIAGE"), eq("QueueTeamUUID"),
            eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamTriageEscalate");
    }

    @Test
    public void whenTriageEscalatedChangeBusinessArea_thenBusAreaStatusIsConfirmed() {
        whenChangeBusinessArea_thenBusAreaStatusIsConfirmed("MPAM_TRIAGE_ESCALATE", "Service_UpdateTeamForTriage",
            "MPAM_TRIAGE", "EndEvent_MpamTriageEscalate", processScenario, bpmnService);
    }

    @Test
    public void whenTriageUpdateEnquiryReasonSubject_thenBusAreaStatusIsConfirmed() {
        ProcessExpressions.registerCallActivityMock("MPAM_UPDATE_ENQUIRY_SUBJECT_REASON").deploy(rule);

        whenUpdateEnquirySubjectReason_thenShouldContinue("MPAM_TRIAGE_ESCALATE", "TriageEscalateOutcome",
            "EndEvent_MpamTriageEscalate", processScenario, bpmnService);

        verify(processScenario).hasCompleted("ServiceTask_0rvjsky");
    }

}
