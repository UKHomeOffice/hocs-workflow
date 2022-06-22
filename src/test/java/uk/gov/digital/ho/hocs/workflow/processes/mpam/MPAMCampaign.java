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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM/MPAM_CAMPAIGN.bpmn")
public class MPAMCampaign {

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
    private ProcessScenario mpamCampaignProcess;

    @Before
    public void defaultScenario() {

        Mocks.register("bpmnService", bpmnService);

        when(mpamCampaignProcess.waitsAtUserTask("UserTask_1w7ywzh"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CampaignOutcome", "SendToTriage")));
    }

    @Test
    public void updateTeamForTriage() {
        when(mpamCampaignProcess.waitsAtUserTask("UserTask_1w7ywzh"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD", "CampaignOutcome", "SendToTriage")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CampaignOutcome", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CampaignOutcome", "SendToTriage")));

        Scenario.run(mpamCampaignProcess)
                .startByKey("MPAM_CAMPAIGN")
                .execute();

        verify(mpamCampaignProcess)
                .hasCompleted("ServiceTask_04a2tzx");
        verify(mpamCampaignProcess, times(3))
                .hasCompleted("ServiceTask_0win31c");
        verify(mpamCampaignProcess)
                .hasFinished("EndEvent_1kgemui");

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRIAGE"), any(), any(), any(), any());
    }

    @Test
    public void updateTeamForDraft() {
        when(mpamCampaignProcess.waitsAtUserTask("UserTask_1w7ywzh"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CampaignOutcome", "SendToDraft")));

        Scenario.run(mpamCampaignProcess)
                .startByKey("MPAM_CAMPAIGN")
                .execute();

        verify(mpamCampaignProcess)
                .hasCompleted("ServiceTask_0jlcf8t");
        verify(mpamCampaignProcess)
                .hasCompleted("ServiceTask_0win31c");
        verify(mpamCampaignProcess)
                .hasFinished("EndEvent_1kgemui");

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), any(), any(), any(), any());
    }

    @Test
    public void updateEnquiryReason(){
        when(mpamCampaignProcess.waitsAtUserTask("UserTask_1w7ywzh"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "UpdateEnquirySubject")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "UpdateEnquirySubject")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "UpdateEnquirySubject")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CampaignOutcome", "SendToDraft")));

        when(mpamCampaignProcess.waitsAtUserTask("UserTask_1mzmra9"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(mpamCampaignProcess.waitsAtUserTask("UserTask_17vuzoo"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        Scenario.run(mpamCampaignProcess)
                .startByKey("MPAM_CAMPAIGN")
                .execute();

        verify(mpamCampaignProcess)
                .hasFinished("EndEvent_1kgemui");

    }

}
