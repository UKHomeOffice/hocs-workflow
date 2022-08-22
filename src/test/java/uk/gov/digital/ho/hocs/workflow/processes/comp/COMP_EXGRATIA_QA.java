package uk.gov.digital.ho.hocs.workflow.processes.comp;

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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP/COMP_EXGRATIA_QA.bpmn")
public class COMP_EXGRATIA_QA {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario exGratiaProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testAcceptRoute(){
        when(exGratiaProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctQaResult", "Accept")));

        Scenario.run(exGratiaProcess).startByKey("COMP_EXGRATIA_QA").execute();

        verify(exGratiaProcess, times(2)).hasCompleted("Screen_Input");
        verify(exGratiaProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Send");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_EXGRATIA_SEND"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }

    @Test
    public void testRejectRoute(){
        when(exGratiaProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctQaResult", "Reject")));

        when(exGratiaProcess.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "valid", false)))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "valid", true, "CaseNote_QaReject", "Rejection")));

        Scenario.run(exGratiaProcess).startByKey("COMP_EXGRATIA_QA").execute();

        verify(exGratiaProcess, times(3)).hasCompleted("Screen_Input");
        verify(exGratiaProcess, times(3)).hasCompleted("Screen_Reject");
        verify(exGratiaProcess).hasCompleted("Service_UpdateAllocationNote_Reject");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Rejection"), eq("REJECT"));
        verify(exGratiaProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_EXGRATIA_RESPONSE_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }

    @Test
    public void testRejectBackAcceptRoute(){
        when(exGratiaProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctQaResult", "Reject")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctQaResult", "Accept")));

        when(exGratiaProcess.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")));

        Scenario.run(exGratiaProcess).startByKey("COMP_EXGRATIA_QA").execute();

        verify(exGratiaProcess, times(2)).hasCompleted("Screen_Input");
        verify(exGratiaProcess).hasCompleted("Screen_Reject");
        verify(exGratiaProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Send");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_EXGRATIA_SEND"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
        verify(exGratiaProcess, never()).hasCompleted("Service_UpdateAllocationNote_Reject");
        verify(bpmnService, never()).updateAllocationNote(any(), any(), eq("Rejection"), eq("REJECT"));
        verify(exGratiaProcess, never()).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
        verify(bpmnService, never()).updateTeamByStageAndTexts(any(), any(), eq("COMP_EXGRATIA_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }
}
