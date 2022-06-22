package uk.gov.digital.ho.hocs.workflow.processes.dcu;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/DCU/DCU_MIN_MARKUP.bpmn")
public class DCU_MIN_Markup extends DCU_MIN_DTEN_Markup_Common {

    // start/end
    public static final String DCU_MIN_MARKUP = "DCU_MIN_MARKUP";
    public static final String DCU_MIN_MARKUP_END = "DCU_MIN_MARKUP_END";

    // common tasks
    public static final String DCU_MARKUP_DECISION = "DCU_MARKUP_DECISION";
    public static final String VALIDATE_DCU_MARKUP_DECISION = "VALIDATE_DCU_MARKUP_DECISION";
    public static final String TOPICS = "TOPICS";
    public static final String VALIDATE_TOPICS = "VALIDATE_TOPICS";
    public static final String UPDATE_HOME_SEC_DEADLINE = "UPDATE_HOME_SEC_DEADLINE";
    public static final String UPDATE_HOME_SEC_STAGES_DEADLINES = "UPDATE_HOME_SEC_STAGES_DEADLINES";
    public static final String UPDATE_STAGE_DEADLINES_FOR_MINISTER_OR_DIRECTOR_TEAMS =
            "UPDATE_STAGE_DEADLINES_FOR_MINISTER_OR_DIRECTOR_TEAMS";
    public static final String UPDATE_MINISTER_OR_DIRECTOR_DEADLINE = "UPDATE_MINISTER_OR_DIRECTOR_DEADLINE";

    // FAQ path tasks
    public static final String DCU_ANSWERING_FAQ = "ServiceTask_0bdoeaw";
    public static final String VALIDATE_DCU_ANSWERING_FAQ = "VALIDATE_DCU_ANSWERING_FAQ";
    public static final String SAVE_PRIMARY_TOPIC_FAQ = "ServiceTask_1qzc8nw";
    public static final String SET_DEFAULT_POLICY_TEAM_FAQ = "ServiceTask_01mkxlc";
    public static final String UPDATE_TEAMS_FOR_TOPICS_FAQ_TEAM_FAQ = "ServiceTask_0i4k7uv";

    // PR path tasks
    public static final String DCU_ANSWERING_PR = "DCU_ANSWERING";
    public static final String VALIDATE_DCU_ANSWERING_PR = "VALIDATE_DCU_ANSWERING_PR";
    public static final String SAVE_PRIMARY_TOPIC_PR = "ServiceTask_0dhbdv9";
    public static final String SET_DEFAULT_POLICY_TEAM_PR = "ServiceTask_077fygw";
    public static final String UPDATE_TEAMS_FOR_TOPICS_PO_TEAM_PR = "ServiceTask_082gadk";
    public static final String UPDATE_TEAMS_FOR_TOPICS_DRAFTING_TEAM_PR = "ServiceTask_1wwfdzr";

    // NRN path tasks (no response needed)
    public static final String NRN_DETAILS = "ServiceTask_1rqyv5a";
    public static final String VALIDATE_NRN_DETAILS = "ServiceTask_1rqyv9f";
    public static final String SAVE_ALLOCATION_NAMES = "ServiceTask_19v0oal";

    // OGD path (other government department)
    public static final String OGD_DETAILS = "ServiceTask_1rqyv3v";
    public static final String VALIDATE_OGD_DETAILS = "UserTask_0d1n3uq";
    public static final String SAVE_ALLOCATION_NODE_OGD = "ServiceTask_0320mje";

    // rej tasks
    public static final String REJ_DETAILS = "ServiceTask_1txj53o";
    public static final String VALIDATE_REJ_DETAILS = "UserTask_059efuh";
    public static final String SAVE_ALLOCATION_NODE_REJ = "ServiceTask_0qill34";

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
    private ProcessScenario dcuMinMarkup;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void faqHappyPath_POTeamIsHomeSec_With_No_OverridePOTeam() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "FAQ")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_TOPICS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_ANSWERING_FAQ))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "OverridePOTeamUUID", "",
                        "POTeamUUID", HOME_SEC_OFFICE)));

        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verifyHomeSecOfficeDeadlinesSet();
        verifyCommonFaqAndPrSteps();

        verify(dcuMinMarkup).hasCompleted(SAVE_PRIMARY_TOPIC_FAQ);
        verify(dcuMinMarkup).hasCompleted(SET_DEFAULT_POLICY_TEAM_FAQ);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_FAQ_TEAM_FAQ);
        verify(dcuMinMarkup).hasCompleted(DCU_ANSWERING_FAQ);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_ANSWERING_FAQ);
        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    @Test
    public void faqHappyPath_OverrideTeamIsHomeSec() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "FAQ")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_TOPICS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_ANSWERING_FAQ))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "OverridePOTeamUUID", HOME_SEC_OFFICE,
                        "POTeamUUID", "not the home sec office")));

        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verifyCommonFaqAndPrSteps();
        verifyHomeSecOfficeDeadlinesSet();

        verify(dcuMinMarkup).hasCompleted(SAVE_PRIMARY_TOPIC_FAQ);
        verify(dcuMinMarkup).hasCompleted(SET_DEFAULT_POLICY_TEAM_FAQ);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_FAQ_TEAM_FAQ);
        verify(dcuMinMarkup).hasCompleted(DCU_ANSWERING_FAQ);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_ANSWERING_FAQ);
        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    @Test
    public void faqHappyPath_NeitherPOTeamNorPOTeamIsHomeSec() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "FAQ")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_TOPICS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_ANSWERING_FAQ))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "OverridePOTeamUUID", "not the home sec office",
                        "POTeamUUID", "not the home sec office")));

        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verifyCommonFaqAndPrSteps();
        verifyMinisterOrDirectorOfficeDeadlinesSet();

        verify(dcuMinMarkup).hasCompleted(SAVE_PRIMARY_TOPIC_FAQ);
        verify(dcuMinMarkup).hasCompleted(SET_DEFAULT_POLICY_TEAM_FAQ);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_FAQ_TEAM_FAQ);
        verify(dcuMinMarkup).hasCompleted(DCU_ANSWERING_FAQ);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_ANSWERING_FAQ);
        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    @Test
    public void prHappyPath_POTeamIsHomeSec_With_No_OverridePOTeam() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "PR")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_TOPICS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_ANSWERING_PR))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "OverridePOTeamUUID", "",
                        "POTeamUUID", HOME_SEC_OFFICE)));

        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verifyCommonFaqAndPrSteps();
        verifyHomeSecOfficeDeadlinesSet();

        verify(dcuMinMarkup).hasCompleted(SAVE_PRIMARY_TOPIC_PR);
        verify(dcuMinMarkup).hasCompleted(SET_DEFAULT_POLICY_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_PO_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_DRAFTING_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(DCU_ANSWERING_PR);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_ANSWERING_PR);
        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    @Test
    public void prHappyPath_OverrideTeamIsHomeSec() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "PR")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_TOPICS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_ANSWERING_PR))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "OverridePOTeamUUID", HOME_SEC_OFFICE,
                        "POTeamUUID", "not the home sec office")));

        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verifyHomeSecOfficeDeadlinesSet();

        verify(dcuMinMarkup).hasCompleted(SAVE_PRIMARY_TOPIC_PR);
        verify(dcuMinMarkup).hasCompleted(SET_DEFAULT_POLICY_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_PO_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_DRAFTING_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(DCU_ANSWERING_PR);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_ANSWERING_PR);
        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    @Test
    public void prHappyPath_NeitherPOTeamNorPOTeamIsHomeSec() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "PR")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_TOPICS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_ANSWERING_PR))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseUUID", CASE_UUID,
                        "OverridePOTeamUUID", "not the home sec office",
                        "POTeamUUID", "not the home sec office")));

        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verifyCommonFaqAndPrSteps();
        verifyMinisterOrDirectorOfficeDeadlinesSet();

        verify(dcuMinMarkup).hasCompleted(SAVE_PRIMARY_TOPIC_PR);
        verify(dcuMinMarkup).hasCompleted(SET_DEFAULT_POLICY_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_PO_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(UPDATE_TEAMS_FOR_TOPICS_DRAFTING_TEAM_PR);
        verify(dcuMinMarkup).hasCompleted(DCU_ANSWERING_PR);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_ANSWERING_PR);
        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    @Test
    public void nrnHappyPath_NeitherPOTeamNorPOTeamIsHomeSec() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "NRN")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_NRN_DETAILS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_MARKUP_DECISION);
        verify(dcuMinMarkup).hasCompleted(NRN_DETAILS);
        verify(dcuMinMarkup).hasCompleted(SAVE_ALLOCATION_NAMES);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    @Test
    public void ogdHappyPath_NeitherPOTeamNorPOTeamIsHomeSec() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "OGD")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_OGD_DETAILS))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_MARKUP_DECISION);
        verify(dcuMinMarkup).hasCompleted(OGD_DETAILS);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_OGD_DETAILS);
        verify(dcuMinMarkup).hasCompleted(SAVE_ALLOCATION_NODE_OGD);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    @Test
    public void rejHappyPath_NeitherPOTeamNorPOTeamIsHomeSec() {
        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_DCU_MARKUP_DECISION))
                .thenReturn(task -> task.complete(withVariables("valid", true,
                        "MarkupDecision", "REJ")));

        when(dcuMinMarkup.waitsAtUserTask(VALIDATE_REJ_DETAILS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));


        Scenario.run(dcuMinMarkup)
                .startByKey(DCU_MIN_MARKUP)
                .execute();

        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_MARKUP_DECISION);
        verify(dcuMinMarkup).hasCompleted(REJ_DETAILS);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_REJ_DETAILS);
        verify(dcuMinMarkup).hasCompleted(SAVE_ALLOCATION_NODE_REJ);

        verify(dcuMinMarkup)
                .hasFinished(DCU_MIN_MARKUP_END);
    }

    private void verifyCommonFaqAndPrSteps() {
        verify(dcuMinMarkup).hasCompleted(DCU_MARKUP_DECISION);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_DCU_MARKUP_DECISION);
        verify(dcuMinMarkup).hasCompleted(TOPICS);
        verify(dcuMinMarkup).hasCompleted(VALIDATE_TOPICS);
    }

    private void verifyHomeSecOfficeDeadlinesSet() {
        verify(dcuMinMarkup).hasCompleted(UPDATE_HOME_SEC_DEADLINE);

        // Case deadline of 10 days
        verify(bpmnService).updateDeadlineDays(eq(CASE_UUID), any(), eq("10"));
        verify(dcuMinMarkup).hasCompleted(UPDATE_HOME_SEC_STAGES_DEADLINES);

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
        verify(dcuMinMarkup).hasCompleted(UPDATE_MINISTER_OR_DIRECTOR_DEADLINE);

        // Case deadline of 20 days
        verify(bpmnService).updateDeadlineDays(eq(CASE_UUID), any(), eq("20"));
        verify(dcuMinMarkup).hasCompleted(UPDATE_STAGE_DEADLINES_FOR_MINISTER_OR_DIRECTOR_TEAMS);

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
    public void shouldSaveDraftAndPolicyTeamWhenPRAndWhenNotOverride() {
        shouldSaveDraftAndPolicyTeamWhenPRAndWhenNotOverride(dcuMinMarkup, "DCU_MIN_MARKUP", bpmnService, "DCU_MIN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenPRAndWhenOverride() {
        shouldSaveDraftAndPolicyTeamWhenPRAndWhenOverride(dcuMinMarkup, "DCU_MIN_MARKUP", bpmnService, "DCU_MIN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenNotOverride() {
        shouldSaveDraftAndPolicyTeamWhenFAQAndWhenNotOverride(dcuMinMarkup, "DCU_MIN_MARKUP", bpmnService, "DCU_MIN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenOverride() {
        shouldSaveDraftAndPolicyTeamWhenFAQAndWhenOverride(dcuMinMarkup, "DCU_MIN_MARKUP", bpmnService, "DCU_MIN");
    }

}
