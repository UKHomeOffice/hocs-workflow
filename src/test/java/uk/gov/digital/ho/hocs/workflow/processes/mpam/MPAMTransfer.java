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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM/MPAM_TRANSFER.bpmn")
public class MPAMTransfer {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.93)
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
    public void whenTransferReject_thenGoBack_thenRepeatAndProceed() {
        when(processScenario.waitsAtUserTask("Validate_UserInput_Transfer"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "TransferOutcome", "TransferRejected")));

        when(processScenario.waitsAtUserTask("Validate_UserInput_RejectUpdateBusinessArea"))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", true)));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRANSFER")
                .execute();

        verify(processScenario, times(2)).hasCompleted("Activity_0isctk7");
        verify(processScenario, times(2)).hasCompleted("Activity_15jdyd3");
        verify(processScenario, times(2)).hasCompleted("Activity_18fmvw3");
        verify(bpmnService, times(1)).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRIAGE"), eq("QueueTeamUUID"),
                eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamTransfer");
    }

    @Test
    public void whenTransferAccepted_thenProceed() {
        when(processScenario.waitsAtUserTask("Validate_UserInput_Transfer"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "TransferOutcome", "TransferAccepted")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRANSFER")
                .execute();

        verify(processScenario).hasFinished("EndEvent_MpamTransfer");
    }

    @Test
    public void whenDeadlineForTransfer_thenProceed() {
        when(processScenario.waitsAtUserTask("Validate_UserInput_Transfer"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "TransferOutcome", "SaveDeadline")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRANSFER")
                .execute();

        verify(processScenario, times(1)).hasCompleted("Activity_1ogqztp");
        verify(processScenario).hasFinished("EndEvent_MpamTransfer");
    }
}
