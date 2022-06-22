package uk.gov.digital.ho.hocs.workflow.processes.mpam;

import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MPAMCommonTests {

    void whenChangeBusinessArea_thenBusAreaStatusIsConfirmed(String mpamFlow, String teamUpdateServiceCall, String teamUpdateValue,
                                                             String endEvent, ProcessScenario processScenario, BpmnService bpmnService) {
        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "BusArea", "",
                        "DIRECTION", "UpdateBusinessArea")));
        when(processScenario.waitsAtUserTask("Validate_BusinessAreaChange"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey(mpamFlow)
                .execute();

        verify(bpmnService).updateValue(any(), any(), eq("BusAreaStatus"), eq("Confirm"));
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq(teamUpdateValue), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasCompleted("Service_SetBusAreaStatusConfirm");
        verify(processScenario).hasCompleted(teamUpdateServiceCall);
        verify(processScenario).hasFinished(endEvent);
    }

    void whenUpdateEnquirySubjectReason_thenShouldContinue(String mpamFlow, String coreVariable, String endEvent,
                                                                     ProcessScenario processScenario, BpmnService bpmnService) {
        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "UpdateEnquirySubject")))
                .thenReturn(task -> task.complete(withVariables(
                    "valid", true,
                    coreVariable, "TEST_COMPLETE",
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey(mpamFlow)
                .execute();

        verify(processScenario).hasFinished(endEvent);
    }

}
