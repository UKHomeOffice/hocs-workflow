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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM_TRIAGE_ESCALATE.bpmn")
public class MPAMTriageEscalate {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.25)
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
    public void whenTransferToOGD_thenAddTransferNote_thenSetDueDate_thenUpdateTeamForTransfer() {

        when(processScenario.waitsAtUserTask("UserTask_1j9nzm5"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateBusinessArea")));

        when(processScenario.waitsAtUserTask("UserTask_15xxyjd"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "BusArea", "TransferToOgd")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE_ESCALATE")
                .execute();

        verify(processScenario).hasCompleted("Activity_0u4xxk6"); // create transfer note
        verify(processScenario).hasCompleted("Activity_0osk3xt"); // set transfer date
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRANSFER"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_132ofai");
    }

    @Test
    public void whenTransferToOther_thenAddTransferNote_thenSetDueDate_thenUpdateTeamForTransfer() {

        when(processScenario.waitsAtUserTask("UserTask_1j9nzm5"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateBusinessArea")));

        when(processScenario.waitsAtUserTask("UserTask_15xxyjd"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "BusArea", "TransferToOther")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE_ESCALATE")
                .execute();

        verify(processScenario).hasCompleted("Activity_0u4xxk6"); // create transfer note
        verify(processScenario).hasCompleted("Activity_0osk3xt"); // set transfer date
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRANSFER"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_132ofai");
    }

    @Test
    public void whenNotTransferToOther_thenUpdateTeamForDraft() {

        when(processScenario.waitsAtUserTask("UserTask_1j9nzm5"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateBusinessArea")));

        when(processScenario.waitsAtUserTask("UserTask_15xxyjd"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "BusArea", "NotTransferToOther")));

        Scenario.run(processScenario)
                .startByKey("MPAM_TRIAGE_ESCALATE")
                .execute();

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_TRIAGE"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_132ofai");
    }

}
