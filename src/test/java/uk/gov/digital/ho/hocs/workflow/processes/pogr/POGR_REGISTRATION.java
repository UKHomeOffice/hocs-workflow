package uk.gov.digital.ho.hocs.workflow.processes.pogr;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/POGR/POGR_REGISTRATION.bpmn" })
public class POGR_REGISTRATION {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

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
    public void testHmpoHappyPath() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO")));

        when(processScenario.waitsAtUserTask("Screen_Hmpo_DataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD", "BusinessArea", "HMPO")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")));

        when(processScenario.waitsAtUserTask("Screen_SendInterimLetter"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")));

        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "HMPO")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "HMPO")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BusinessSelect");
        verify(processScenario).hasCompleted("Screen_BusinessAreaSelect");
        verify(processScenario, times(2)).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario, times(2)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario, times(3)).hasCompleted("Screen_Hmpo_DataInput");
        verify(processScenario, times(2)).hasCompleted("Screen_SendInterimLetter");
        verify(processScenario).hasCompleted("EndEvent_BusinessSelect");

        verify(bpmnService, times(2)).updateDeadlineDays(any(), any(), eq("10"));
    }

    @Test
    public void testHappyGroPath() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO")));

        when(processScenario.waitsAtUserTask("Screen_SendInterimLetter"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE")));

        when(processScenario.waitsAtUserTask("Screen_GroAllocateTeam"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE")));

        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "GRO")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "GRO")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DIRECTION", "BACKWARD", "BusinessArea", "GRO", "ComplaintPriority", "", "ComplaintChannel", "")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE", "ComplaintPriority", "", "ComplaintChannel", "Email")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE", "ComplaintPriority", "", "ComplaintChannel", "Email")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BusinessSelect");
        verify(processScenario).hasCompleted("Screen_BusinessAreaSelect");
        verify(processScenario, times(2)).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario, times(2)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario, times(3)).hasCompleted("CallActivity_Gro_DataInput");
        verify(processScenario, times(3)).hasCompleted("Screen_SendInterimLetter");
        verify(processScenario, times(2)).hasCompleted("Screen_GroAllocateTeam");
        verify(processScenario).hasCompleted("Service_SetGroTriageTeam");
        verify(processScenario).hasCompleted("EndEvent_BusinessSelect");

        verify(bpmnService, times(2)).updateDeadlineDays(any(), any(), eq("5"));
    }

    @Test
    public void testSwapBetweenBusinessAreas() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO")));

        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "BACKWARD", "BusinessArea", "GRO")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "HMPO")
                .deploy(rule);

        when(processScenario.waitsAtUserTask("Screen_Hmpo_DataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO")));

        when(processScenario.waitsAtUserTask("Screen_SendInterimLetter"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")));

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BusinessSelect");
        verify(processScenario, times(2)).hasCompleted("Screen_BusinessAreaSelect");
        verify(processScenario).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario, times(2)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario).hasCompleted("Screen_Hmpo_DataInput");
        verify(processScenario).hasCompleted("Screen_SendInterimLetter");
        verify(processScenario).hasCompleted("EndEvent_BusinessSelect");

        verify(bpmnService).updateDeadlineDays(any(), any(), eq("10"));
    }

    @Test
    public void testBackwardAfterDataInputSubmission() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO")));

        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "FORWARD","BusinessArea", "HMPO")
                .thenReturn("DIRECTION", "BACKWARD","BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")
                .thenReturn("DIRECTION", "FORWARD","BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")
                .deploy(rule);

        when(processScenario.waitsAtUserTask("Screen_Hmpo_DataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")));

        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelectPostDataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")));

        when(processScenario.waitsAtUserTask("Screen_SendInterimLetter"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO", "XPogrPostDataInput", "TRUE")));

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_BusinessSelect");
        verify(processScenario).hasCompleted("Screen_BusinessAreaSelect");
        verify(processScenario, times(2)).hasCompleted("Service_UpdateCaseDeadline");
        verify(processScenario, times(3)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario, times(3)).hasCompleted("Screen_Hmpo_DataInput");
        verify(processScenario, times(2)).hasCompleted("Screen_SendInterimLetter");
        verify(processScenario).hasCompleted("Screen_BusinessAreaSelectPostDataInput");
        verify(processScenario).hasCompleted("EndEvent_BusinessSelect");

        verify(bpmnService, times(2)).updateDeadlineDays(any(), any(), eq("10"));
    }

    @Test
    public void testDeadlineSetToTenDaysForGroPostChannel() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO")));

        when(processScenario.waitsAtUserTask("Screen_SendInterimLetter"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE")));

        when(processScenario.waitsAtUserTask("Screen_GroAllocateTeam"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE")));

        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "GRO")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE", "ComplaintPriority", "", "ComplaintChannel", "Post")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("Service_UpdateCaseDeadline");
        verify(bpmnService, times(1)).updateDeadlineDays(any(), any(), eq("10"));
    }

    @Test
    public void testDeadlineSetToOneDayForGroPriority() {
        when(processScenario.waitsAtUserTask("Screen_BusinessAreaSelect"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO")));

        when(processScenario.waitsAtUserTask("Screen_SendInterimLetter"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE")));

        when(processScenario.waitsAtUserTask("Screen_GroAllocateTeam"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE")));

        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "GRO")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DIRECTION", "FORWARD", "BusinessArea", "GRO", "XPogrPostDataInput", "TRUE", "ComplaintPriority", "Yes", "ComplaintChannel", "Post")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_REGISTRATION")
                .execute();

        verify(processScenario).hasCompleted("Service_UpdateCaseDeadline");
        verify(bpmnService, times(1)).updateDeadlineDays(any(), any(), eq("1"));
    }
}
