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

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/DCU_MIN_PRIVATE_OFFICE.bpmn")
public class DCUMinPrivateOffice {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.2)
            .build();

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
                        "Topics", topicUUID.toString())));

        Scenario.run(processScenario)
                .startByKey("DCU_MIN_PRIVATE_OFFICE")
                .execute();

        verify(processScenario, times(4)).hasCompleted("Screen_ApprovePrivateOffice");
        verify(processScenario, times(3)).hasCompleted("Screen_ChangeTopic");
        verify(processScenario).hasCompleted("Service_SavePrimaryTopic");
        verify(bpmnService).updatePrimaryTopic(any(), any(), eq(topicUUID.toString()));
        verify(processScenario).hasFinished("DCU_MIN_PRIVATE_OFFICE_END");
    }
}
