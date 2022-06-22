package uk.gov.digital.ho.hocs.workflow.processes.comp;

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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP/COMP_SERVICE_QA.bpmn")
public class COMP_SERVICE_QA {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testAcceptRoute(){
        when(processScenario.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctQaResult", "Accept")));

        Scenario.run(processScenario).startByKey("COMP_SERVICE_QA").execute();

        verify(processScenario, times(2)).hasCompleted("Screen_Input");
        verify(processScenario).hasCompleted("Service_UpdateTeamByStageAndTexts_Send");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_SERVICE_SEND"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }

    @Test
    public void testRejectRoute(){
        when(processScenario.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctQaResult", "Reject")));

        when(processScenario.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "valid", false)))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "valid", true, "CaseNote_QaReject", "Rejection")));

        Scenario.run(processScenario).startByKey("COMP_SERVICE_QA").execute();

        verify(processScenario, times(3)).hasCompleted("Screen_Input");
        verify(processScenario, times(3)).hasCompleted("Screen_Reject");
        verify(processScenario).hasCompleted("Service_UpdateAllocationNote_Reject");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Rejection"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_SERVICE_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }

    @Test
    public void testRejectBackAcceptRoute(){
        when(processScenario.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctQaResult", "Reject")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctQaResult", "Accept")));

        when(processScenario.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")));

        Scenario.run(processScenario).startByKey("COMP_SERVICE_QA").execute();

        verify(processScenario, times(2)).hasCompleted("Screen_Input");
        verify(processScenario).hasCompleted("Screen_Reject");
        verify(processScenario).hasCompleted("Service_UpdateTeamByStageAndTexts_Send");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_SERVICE_SEND"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
        verify(processScenario, never()).hasCompleted("Service_UpdateAllocationNote_Reject");
        verify(bpmnService, never()).updateAllocationNote(any(), any(), eq("Rejection"), eq("REJECT"));
        verify(processScenario, never()).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
        verify(bpmnService, never()).updateTeamByStageAndTexts(any(), any(), eq("COMP_SERVICE_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }
}
