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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/MPAM.bpmn",
        "processes/MPAM_CREATION.bpmn",
        "processes/MPAM_TRIAGE.bpmn",
        "processes/MPAM_DRAFT.bpmn",
        "processes/MPAM_PO.bpmn",
        "processes/MPAM_QA.bpmn",
        "processes/MPAM_DRAFT_REQUESTED_CONTRIBUTION.bpmn",
        "processes/MPAM_DRAFT_ESCALATE.bpmn",
        "processes/MPAM_CAMPAIGN.bpmn",
        "processes/STAGE.bpmn",
        "processes/STAGE_WITH_USER.bpmn"})
public class MPAM {

    public static final String DRAFT_REQUEST_CONTRIBUTION = "CallActivity_1068j5g";
    public static final String DRAFT_REQUEST_CONTRIBUTION_RESULT = "ExclusiveGateway_1hlhu8m";
    public static final String DRAFT_REQUEST_CONTRIBUTION_ESCALATED = "Activity_134af50";

    public static final String DRAFT_REQUEST_CONTRIBUTION_ESCALATED_RESULT = "Gateway_0kabhfi";
    public static final String DRAFT_ESCALATE_DRAFT_STATUS = "ExclusiveGateway_1nyjaew";
    public static final String CAMPAIGN = "CallActivity_0l0wizp";

    public static final String DRAFT_CLEAR_USER = "Activity_1l35uib";

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

        whenAtCallActivity("MPAM_CREATION")
                .deploy(rule);

        whenAtCallActivity("MPAM_TRIAGE")
                .deploy(rule);

        whenAtCallActivity("MPAM_DRAFT")
                .alwaysReturn("RefType", "Ministerial")
                .deploy(rule);

        whenAtCallActivity("MPAM_QA")
                .deploy(rule);

        whenAtCallActivity("MPAM_PO")
                .alwaysReturn("PoStatus", "Dispatched-Follow-Up")
                .deploy(rule);

        whenAtCallActivity("MPAM_DISPATCHED_FOLLOW_UP")
                .deploy(rule);
    }

    @Test
    public void happyPath() {

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_1c4zy60");
        verify(mpamProcess)
                .hasCompleted("ServiceTask_0rwk9ie");

    }

    @Test
    public void putInCampaign_fromContributionRequestedEscalation() {

        whenAtCallActivity("MPAM_DRAFT")
                .thenReturn("DraftStatus", "RequestContribution")
                .thenReturn("DraftStatus", "", "RefType", "Ministerial")
                .deploy(rule);

        whenAtCallActivity("MPAM_DRAFT_REQUESTED_CONTRIBUTION")
                .thenReturn("DraftRequestedContributionOutcome", "Escalate")
                .deploy(rule);

        whenAtCallActivity("MPAM_DRAFT_ESCALATE")
                .thenReturn("DraftStatus", "PutOnCampaign")
                .deploy(rule);

        whenAtCallActivity("MPAM_CAMPAIGN")
                .thenReturn("CampaignOutcome", "SendToDraft")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_RESULT);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_ESCALATED);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_ESCALATED_RESULT);
        verify(mpamProcess)
                .hasCompleted(DRAFT_ESCALATE_DRAFT_STATUS);
        verify(mpamProcess)
                .hasCompleted(CAMPAIGN);
    }

    @Test
    public void removeDraftUser_fromContributionRequested_specifyUnallocate() {

        whenAtCallActivity("MPAM_DRAFT")
                .thenReturn("DraftStatus", "RequestContribution")
                .thenReturn("DraftStatus", "", "RefType", "Ministerial")
                .deploy(rule);

        whenAtCallActivity("MPAM_DRAFT_REQUESTED_CONTRIBUTION")
                .thenReturn("DraftRequestedContributionOutcome", "Complete", "DraftShouldUnallocate", "Unallocate")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_RESULT);
        verify(mpamProcess)
                .hasCompleted(DRAFT_CLEAR_USER);
    }

    @Test
    public void removeDraftUser_fromContributionRequested_specifyRetain() {

        whenAtCallActivity("MPAM_DRAFT")
                .thenReturn("DraftStatus", "RequestContribution")
                .thenReturn("DraftStatus", "", "RefType", "Ministerial")
                .deploy(rule);

        whenAtCallActivity("MPAM_DRAFT_REQUESTED_CONTRIBUTION")
                .thenReturn("DraftRequestedContributionOutcome", "Complete", "DraftShouldUnallocate", "Retail")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_RESULT);
        verify(mpamProcess, never())
                .hasCompleted(DRAFT_CLEAR_USER);
    }
}
