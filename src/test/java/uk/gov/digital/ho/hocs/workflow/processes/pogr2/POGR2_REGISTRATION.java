package uk.gov.digital.ho.hocs.workflow.processes.pogr2;

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
import java.util.Map;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/POGR2/POGR2_REGISTRATION.bpmn" })
public class POGR2_REGISTRATION {

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
        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "")
                .thenReturn("DIRECTION", "FORWARD")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        when(processScenario.waitsAtUserTask("Screen_Hmpo_DataInput"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "", "BusinessArea", "HMPO")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD", "BusinessArea", "HMPO")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "BusinessArea", "HMPO")));

        Scenario.run(processScenario)
                .startByKey("POGR2_REGISTRATION", Map.of("BusinessArea", "HMPO"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Registration");
        verify(processScenario, times(3)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario, times(3)).hasCompleted("Screen_Hmpo_DataInput");
        verify(processScenario).hasCompleted("EndEvent_Registration");
    }

    @Test
    public void testHappyGroPath() {
        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "BACKWARD")
                .thenReturn("DIRECTION", "FORWARD")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DIRECTION", "")
                .thenReturn("DIRECTION", "BACKWARD")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR2_REGISTRATION", Map.of("BusinessArea", "GRO", "ComplaintChannel", "Other", "ComplaintPriority", "No"))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Registration");
        verify(processScenario).hasCompleted("Service_UpdateCaseDeadline");
        verify(bpmnService).updateDeadlineDays(any(), any(), eq("5"));
        verify(processScenario, times(3)).hasCompleted("CallActivity_CorrespondentInput");
        verify(processScenario, times(3)).hasCompleted("CallActivity_Gro_DataInput");
        verify(processScenario).hasCompleted("EndEvent_Registration");
    }

    @Test
    public void testDeadlineSetToTenDaysForGroPostChannel() {
        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR2_REGISTRATION", Map.of("BusinessArea", "GRO","ComplaintChannel", "Post", "ComplaintPriority", "No"))
                .execute();

        verify(bpmnService, times(1)).updateDeadlineDays(any(), any(), eq("10"));
    }

    @Test
    public void testDeadlineSetToOneDayForGroPriority() {
        whenAtCallActivity("COMPLAINT_CORRESPONDENT")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DIRECTION", "FORWARD")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR2_REGISTRATION", Map.of("BusinessArea", "GRO", "ComplaintChannel", "Other", "ComplaintPriority", "Yes"))
                .execute();

        verify(bpmnService, times(1)).updateDeadlineDays(any(), any(), eq("1"));
    }
}
