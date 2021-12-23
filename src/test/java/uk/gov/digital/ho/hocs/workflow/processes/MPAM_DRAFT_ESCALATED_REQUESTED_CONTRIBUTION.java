package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.test.Deployment;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION.bpmn")
public class MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION {

    public static final String DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN = "ServiceTask_0win31c";
    public static final String DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK = "UserTask_1w7ywzh";

    public static final String DRAFT_UNALLOCATE_CASE_SCREEN = "Activity_0c1yifp";
    public static final String DRAFT_UNALLOCATE_CASE_USER_TASK = "Activity_18i0lnc";

    public static final String CAMPAIGN_SCREEN = "ServiceTask_1htucc1";
    public static final String CAMPAIGN_USER_TASK = "UserTask_113u04m";
    public static final String CLEAR_CAMPAIGN_TYPE_TASK = "ServiceTask_0r4781b";
    public static final String UPDATE_TEAM_FOR_CAMPAIGN_TASK = "ServiceTask_075paor";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .build();

    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario mpamProcess;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void happyPath() {
        when(mpamProcess.waitsAtUserTask(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DraftEscalatedRequestedContributionOutcome", "Complete")));

        when(mpamProcess.waitsAtUserTask(DRAFT_UNALLOCATE_CASE_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "DraftShouldUnallocate", "Retain")));

        Scenario.run(mpamProcess)
                .startByKey("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .execute();

        verify(mpamProcess, times(1))
                .hasCompleted(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN);

        verify(mpamProcess, times(1))
                .hasCompleted(DRAFT_UNALLOCATE_CASE_SCREEN);

         verifyNoMoreInteractions(bpmnService);
    }

    @Test
    public void requestContributionScreen_validFalseFirstTime() {
        when(mpamProcess.waitsAtUserTask(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DraftEscalatedRequestedContributionOutcome", "Complete")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DraftEscalatedRequestedContributionOutcome", "Complete")));

        when(mpamProcess.waitsAtUserTask(DRAFT_UNALLOCATE_CASE_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "DraftShouldUnallocate", "Retain")));

        Scenario.run(mpamProcess)
                .startByKey("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .execute();

        verify(mpamProcess, times(2))
                .hasCompleted(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN);

        verify(mpamProcess, times(1))
                .hasCompleted(DRAFT_UNALLOCATE_CASE_SCREEN);
    }

    @Test
    public void unallocateUserScreen_validFalseFirstTime() {
        when(mpamProcess.waitsAtUserTask(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DraftEscalatedRequestedContributionOutcome", "Complete")));

        when(mpamProcess.waitsAtUserTask(DRAFT_UNALLOCATE_CASE_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD",
                        "DraftShouldUnallocate", "Retain")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "DraftShouldUnallocate", "Retain")));

        Scenario.run(mpamProcess)
                .startByKey("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .execute();

        verify(mpamProcess, times(1))
                .hasCompleted(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN);

        verify(mpamProcess, times(2))
                .hasCompleted(DRAFT_UNALLOCATE_CASE_SCREEN);
    }

    @Test
    public void unallocateUserScreen_directionBackwardFirst() {
        when(mpamProcess.waitsAtUserTask(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DraftEscalatedRequestedContributionOutcome", "Complete")));

        when(mpamProcess.waitsAtUserTask(DRAFT_UNALLOCATE_CASE_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "BACKWARD",
                        "DraftShouldUnallocate", "Retain")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "DraftShouldUnallocate", "Retain")));

        Scenario.run(mpamProcess)
                .startByKey("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .execute();

        verify(mpamProcess, times(2))
                .hasCompleted(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN);

        verify(mpamProcess, times(2))
                .hasCompleted(DRAFT_UNALLOCATE_CASE_SCREEN);
    }

    @Test
    public void requestContributionScreen_escalateCase() {
        when(mpamProcess.waitsAtUserTask(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DraftEscalatedRequestedContributionOutcome", "Escalate")));

        Scenario.run(mpamProcess)
                .startByKey("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .execute();

        verify(mpamProcess, times(1))
                .hasCompleted(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN);

        verify(mpamProcess, never())
                .hasCompleted(CLEAR_CAMPAIGN_TYPE_TASK);

        verify(mpamProcess, never())
                .hasCompleted(DRAFT_UNALLOCATE_CASE_SCREEN);

         verifyNoMoreInteractions(bpmnService);
    }

    @Test
    public void requestContributionScreen_putInCampaign() {
        when(mpamProcess.waitsAtUserTask(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DraftEscalatedRequestedContributionOutcome", "PutOnCampaign")));

        when(mpamProcess.waitsAtUserTask(CAMPAIGN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(mpamProcess)
                .startByKey("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .execute();

        verify(mpamProcess, times(1))
                .hasCompleted(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN);

        verify(mpamProcess, times(1))
                .hasCompleted(CLEAR_CAMPAIGN_TYPE_TASK);

        verify(bpmnService).blankCaseValues(any(), any(), eq("CampaignType"));

        verify(mpamProcess, times(1))
                .hasCompleted(CAMPAIGN_SCREEN);

        verify(mpamProcess, times(1))
                .hasCompleted(UPDATE_TEAM_FOR_CAMPAIGN_TASK);

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_CAMPAIGN"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
    }

    @Test
    public void requestContributionScreen_putInCampaign_directionBackwardFirst() {
        when(mpamProcess.waitsAtUserTask(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DraftEscalatedRequestedContributionOutcome", "PutOnCampaign")));

        when(mpamProcess.waitsAtUserTask(CAMPAIGN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(mpamProcess)
                .startByKey("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .execute();

        verify(mpamProcess, times(2))
                .hasCompleted(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN);

        verify(mpamProcess, times(2))
                .hasCompleted(CLEAR_CAMPAIGN_TYPE_TASK);

        verify(bpmnService, times(2))
                .blankCaseValues(any(), any(), eq("CampaignType"));

        verify(mpamProcess, times(2))
                .hasCompleted(CAMPAIGN_SCREEN);

        verify(mpamProcess, times(1))
                .hasCompleted(UPDATE_TEAM_FOR_CAMPAIGN_TASK);

        verify(bpmnService, times(1))
                .updateTeamByStageAndTexts(any(), any(), eq("MPAM_CAMPAIGN"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
    }

    @Test
    public void requestContributionScreen_putInCampaign_validFalseFirstTime() {
        when(mpamProcess.waitsAtUserTask(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DraftEscalatedRequestedContributionOutcome", "PutOnCampaign")));

        when(mpamProcess.waitsAtUserTask(CAMPAIGN_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(mpamProcess)
                .startByKey("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .execute();

        verify(mpamProcess, times(1))
                .hasCompleted(DRAFT_ESCALATED_REQUESTED_CONTRIBUTION_SCREEN);

        verify(mpamProcess, times(1))
                .hasCompleted(CLEAR_CAMPAIGN_TYPE_TASK);

        verify(bpmnService).blankCaseValues(any(), any(), eq("CampaignType"));

        verify(mpamProcess, times(2))
                .hasCompleted(CAMPAIGN_SCREEN);

        verify(mpamProcess, times(1))
                .hasCompleted(UPDATE_TEAM_FOR_CAMPAIGN_TASK);

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_CAMPAIGN"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
    }
}
