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

import java.util.UUID;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/DCU_MIN_PRIVATE_OFFICE.bpmn")
public class DCU_MIN_PRIVATE_OFFICE {

    //common
    public static final String DCU_MIN_PRIVATE_OFFICE = "DCU_MIN_PRIVATE_OFFICE";
    public static final String DCU_MIN_PRIVATE_OFFICE_END = "DCU_MIN_PRIVATE_OFFICE_END";

    public static final String APPROVE_PRIVATE_OFFICE = "Screen_ApprovePrivateOffice";
    public static final String VALIDATE_APPROVE_PRIVATE_OFFICE = "Validate_ApprovePrivateOffice";
    public static final String UPDATE_HOME_SEC_DEADLINE = "UPDATE_HOME_SEC_DEADLINE";
    public static final String UPDATE_HOME_SEC_STAGES_DEADLINES = "UPDATE_HOME_SEC_STAGES_DEADLINES";
    public static final String UPDATE_STAGE_DEADLINES_FOR_MINISTER_OR_DIRECTOR_TEAMS =
            "UPDATE_STAGE_DEADLINES_FOR_MINISTER_OR_DIRECTOR_TEAMS";
    public static final String UPDATE_MINISTER_OR_DIRECTOR_DEADLINE = "UPDATE_MINISTER_OR_DIRECTOR_DEADLINE";

    // Change path
    public static final String CHANGE_MINISTER = "ServiceTask_0li8mup";
    public static final String VALIDATE_CHANGE_MINISTER = "UserTask_1nuanaj";
    public static final String SAVE_ALLOCATION_NOTE_CHANGE = "ServiceTask_0sehc3k";
    public static final String UPDATE_ALLOCATION_TEAMS_CHANGE = "ServiceTask_0l2vtrc";

    // Reject Path
    public static final String REJECTION_NOTE = "ServiceTask_0x8k4ex";
    public static final String VALIDATE_REJECTION_NOTE = "UserTask_0l1q0db";
    public static final String SAVE_ALLOCATION_NOTE_REJECT = "ServiceTask_070xh4g";

    // UUIDS
    public static final String HOME_SEC_OFFICE = "3d2c7893-92c5-4347-804a-8826f06f0c9d";
    public static final String CASE_UUID = "ABC";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.86)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario processScenario;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void acceptHappyPath() {
        when(processScenario.waitsAtUserTask(VALIDATE_APPROVE_PRIVATE_OFFICE))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "PrivateOfficeDecision", "ACCEPT")));

        Scenario.run(processScenario)
                .startByKey(DCU_MIN_PRIVATE_OFFICE)
                .execute();

        verify(processScenario).hasCompleted(APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(VALIDATE_APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasFinished(DCU_MIN_PRIVATE_OFFICE_END);
    }

    @Test
    public void changeHappyPath_POTeamIsHomeSec_With_No_PrivateOfficeOverridePOTeamUUID() {
        when(processScenario.waitsAtUserTask(VALIDATE_APPROVE_PRIVATE_OFFICE))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "PrivateOfficeDecision", "CHANGE")));

        when(processScenario.waitsAtUserTask(VALIDATE_CHANGE_MINISTER))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "PrivateOfficeOverridePOTeamUUID", "",
                        "POTeamUUID", HOME_SEC_OFFICE)));

        Scenario.run(processScenario)
                .startByKey(DCU_MIN_PRIVATE_OFFICE)
                .execute();

        verifyHomeSecOfficeDeadlinesSet();

        verify(processScenario).hasCompleted(APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(VALIDATE_APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(CHANGE_MINISTER);
        verify(processScenario).hasCompleted(VALIDATE_CHANGE_MINISTER);
        verify(processScenario).hasCompleted(SAVE_ALLOCATION_NOTE_CHANGE);
        verify(processScenario).hasCompleted(UPDATE_ALLOCATION_TEAMS_CHANGE);
        verify(processScenario).hasFinished(DCU_MIN_PRIVATE_OFFICE_END);
    }

    @Test
    public void changeHappyPath_OverrideTeamIsHomeSec() {
        when(processScenario.waitsAtUserTask(VALIDATE_APPROVE_PRIVATE_OFFICE))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "PrivateOfficeDecision", "CHANGE")));

        when(processScenario.waitsAtUserTask(VALIDATE_CHANGE_MINISTER))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "PrivateOfficeOverridePOTeamUUID", HOME_SEC_OFFICE,
                        "POTeamUUID", "not the home sec office")));

        Scenario.run(processScenario)
                .startByKey(DCU_MIN_PRIVATE_OFFICE)
                .execute();

        verifyHomeSecOfficeDeadlinesSet();

        verify(processScenario).hasCompleted(APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(VALIDATE_APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(CHANGE_MINISTER);
        verify(processScenario).hasCompleted(VALIDATE_CHANGE_MINISTER);
        verify(processScenario).hasCompleted(SAVE_ALLOCATION_NOTE_CHANGE);
        verify(processScenario).hasCompleted(UPDATE_ALLOCATION_TEAMS_CHANGE);
        verify(processScenario).hasFinished(DCU_MIN_PRIVATE_OFFICE_END);
    }

    @Test
    public void changeHappyPath_NeitherPOTeamNorPOTeamIsHomeSec() {
        when(processScenario.waitsAtUserTask(VALIDATE_APPROVE_PRIVATE_OFFICE))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "PrivateOfficeDecision", "CHANGE")));

        when(processScenario.waitsAtUserTask(VALIDATE_CHANGE_MINISTER))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "PrivateOfficeOverridePOTeamUUID", "not the home sec office",
                        "POTeamUUID", "not the home sec office")));

        Scenario.run(processScenario)
                .startByKey(DCU_MIN_PRIVATE_OFFICE)
                .execute();

        verifyMinisterOrDirectorOfficeDeadlinesSet();

        verify(processScenario).hasCompleted(APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(VALIDATE_APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(CHANGE_MINISTER);
        verify(processScenario).hasCompleted(VALIDATE_CHANGE_MINISTER);
        verify(processScenario).hasCompleted(SAVE_ALLOCATION_NOTE_CHANGE);
        verify(processScenario).hasCompleted(UPDATE_ALLOCATION_TEAMS_CHANGE);
        verify(processScenario).hasFinished(DCU_MIN_PRIVATE_OFFICE_END);
    }

    @Test
    public void rejectHappyPath() {
        when(processScenario.waitsAtUserTask(VALIDATE_APPROVE_PRIVATE_OFFICE))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "PrivateOfficeDecision", "REJECT")));

        when(processScenario.waitsAtUserTask(VALIDATE_REJECTION_NOTE))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey(DCU_MIN_PRIVATE_OFFICE)
                .execute();

        verify(processScenario).hasCompleted(APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(VALIDATE_APPROVE_PRIVATE_OFFICE);
        verify(processScenario).hasCompleted(REJECTION_NOTE);
        verify(processScenario).hasCompleted(VALIDATE_REJECTION_NOTE);
        verify(processScenario).hasCompleted(SAVE_ALLOCATION_NOTE_REJECT);
        verify(processScenario).hasFinished(DCU_MIN_PRIVATE_OFFICE_END);
    }

    private void verifyHomeSecOfficeDeadlinesSet() {
        verify(processScenario).hasCompleted(UPDATE_HOME_SEC_DEADLINE);

        // Case deadline of 10 days
        verify(bpmnService).updateDeadlineDays(eq(CASE_UUID), any(), eq("10"));
        verify(processScenario).hasCompleted(UPDATE_HOME_SEC_STAGES_DEADLINES);

        // Case stage deadlines
        verify(bpmnService).updateDeadlineForStages(
                eq(CASE_UUID),
                any(),
                eq("DCU_MIN_INITIAL_DRAFT"), eq("7"),
                eq("DCU_MIN_QA_RESPONSE"), eq("7"),
                eq("DCU_MIN_PRIVATE_OFFICE"), eq("9"),
                eq("DCU_MIN_MINISTER_SIGN_OFF"), eq("9"),
                eq("DCU_MIN_TRANSFER_CONFIRMATION"), eq("10"),
                eq("DCU_MIN_NO_REPLY_NEEDED_CONFIRMATION"), eq("10"),
                eq("DCU_MIN_DISPATCH"), eq("10"),
                eq("DCU_MIN_COPY_NUMBER_TEN"), eq("10")
        );
    }

    private void verifyMinisterOrDirectorOfficeDeadlinesSet() {
        verify(processScenario).hasCompleted(UPDATE_MINISTER_OR_DIRECTOR_DEADLINE);

        // Case deadline of 20 days
        verify(bpmnService).updateDeadlineDays(eq(CASE_UUID), any(), eq("20"));
        verify(processScenario).hasCompleted(UPDATE_STAGE_DEADLINES_FOR_MINISTER_OR_DIRECTOR_TEAMS);

        // Case stage deadlines
        verify(bpmnService).updateDeadlineForStages(
                eq(CASE_UUID),
                any(),
                eq("DCU_MIN_INITIAL_DRAFT"), eq("10"),
                eq("DCU_MIN_QA_RESPONSE"), eq("10"),
                eq("DCU_MIN_PRIVATE_OFFICE"), eq("19"),
                eq("DCU_MIN_MINISTER_SIGN_OFF"), eq("19"),
                eq("DCU_MIN_TRANSFER_CONFIRMATION"), eq("20"),
                eq("DCU_MIN_NO_REPLY_NEEDED_CONFIRMATION"), eq("20"),
                eq("DCU_MIN_DISPATCH"), eq("20"),
                eq("DCU_MIN_COPY_NUMBER_TEN"), eq("20")
        );
    }

    @Test
    public void whenTopic_thenChangeTopic_andSavePrimaryTopic() {

        UUID topicUUID = UUID.randomUUID();
        when(processScenario.waitsAtUserTask("Validate_ApprovePrivateOffice"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PrivateOfficeDecision", "TOPIC")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PrivateOfficeDecision", "TOPIC")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PrivateOfficeDecision", "ACCEPT")));
        when(processScenario.waitsAtUserTask("Validate_ChangeTopic"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_PrivateOfficeTopic", "Change topic",
                        "Topics", topicUUID.toString())));

        Scenario.run(processScenario)
                .startByKey("DCU_MIN_PRIVATE_OFFICE")
                .execute();

        verify(processScenario, times(4)).hasCompleted("Screen_ApprovePrivateOffice");
        verify(processScenario, times(3)).hasCompleted("Screen_ChangeTopic");
        verify(processScenario).hasCompleted("Service_SavePrimaryTopic");
        verify(bpmnService).updatePrimaryTopic(any(), any(), eq(topicUUID.toString()));
        verify(processScenario).hasCompleted("Service_SaveTopicChangeNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Change topic"), eq("CHANGE"));
        verify(processScenario).hasFinished("DCU_MIN_PRIVATE_OFFICE_END");
    }
}
