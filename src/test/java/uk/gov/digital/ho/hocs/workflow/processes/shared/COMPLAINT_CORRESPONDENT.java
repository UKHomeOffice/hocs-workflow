package uk.gov.digital.ho.hocs.workflow.processes.shared;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import java.util.Map;
import java.util.UUID;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/COMPLAINT_CORRESPONDENT.bpmn" })
public class COMPLAINT_CORRESPONDENT {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

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
    public void testHappyPath() {
        when(processScenario.waitsAtUserTask("Screen_CorrespondentsInput")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(bpmnService.caseHasPrimaryCorrespondentType(any(), any())).thenReturn(true);

        Scenario.run(processScenario).startByKey("COMPLAINT_CORRESPONDENT",
            Map.of("CORRESPONDENT_INPUT_SCREEN", "COMPLAINT_INPUT_SCREEN", "CORRESPONDENT_INVALID_SCREEN",
                "COMPLAINT_INVALID_SCREEN", "CaseUUID", UUID.randomUUID().toString())).execute();

        verify(processScenario).hasCompleted("StartEvent_ComplaintCorrespondent");
        verify(processScenario).hasCompleted("Screen_CorrespondentsInput");
        verify(processScenario).hasCompleted("Service_UpdatePrimaryCorrespondent");
        verify(processScenario).hasCompleted("Service_CaseHasPrimaryCorrespondentType");
        verify(processScenario).hasCompleted("EndEvent_ComplaintCorrespondent");

        verify(bpmnService).updatePrimaryCorrespondent(any(), any(), any());
        verify(bpmnService).caseHasPrimaryCorrespondentType(any(), any());

    }

    @Test
    public void testBackwardsDirectionFromInput() {
        when(processScenario.waitsAtUserTask("Screen_CorrespondentsInput")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD")));

        Scenario.run(processScenario).startByKey("COMPLAINT_CORRESPONDENT",
            Map.of("CORRESPONDENT_INPUT_SCREEN", "COMPLAINT_INPUT_SCREEN", "CORRESPONDENT_INVALID_SCREEN",
                "COMPLAINT_INVALID_SCREEN", "CaseUUID", UUID.randomUUID().toString())).execute();

        verify(processScenario).hasCompleted("StartEvent_ComplaintCorrespondent");
        verify(processScenario).hasCompleted("Screen_CorrespondentsInput");
        verify(processScenario).hasCompleted("EndEvent_ComplaintCorrespondent");

        verify(bpmnService, times(0)).updatePrimaryCorrespondent(any(), any(), any());
    }

    @Test
    public void testPrimaryCorrespondentNotComplainant() {
        when(processScenario.waitsAtUserTask("Screen_CorrespondentsInput")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));
        when(processScenario.waitsAtUserTask("Screen_CorrespondentsInvalid")).thenReturn(TaskDelegate::complete);

        when(bpmnService.caseHasPrimaryCorrespondentType(any(), any())).thenReturn(false).thenReturn(true);

        Scenario.run(processScenario).startByKey("COMPLAINT_CORRESPONDENT",
            Map.of("CORRESPONDENT_INPUT_SCREEN", "COMPLAINT_INPUT_SCREEN", "CORRESPONDENT_INVALID_SCREEN",
                "COMPLAINT_INVALID_SCREEN", "CaseUUID", UUID.randomUUID().toString())).execute();

        verify(processScenario).hasCompleted("StartEvent_ComplaintCorrespondent");

        verify(processScenario, times(2)).hasCompleted("Screen_CorrespondentsInput");
        verify(processScenario, times(2)).hasCompleted("Service_UpdatePrimaryCorrespondent");
        verify(processScenario, times(2)).hasCompleted("Service_CaseHasPrimaryCorrespondentType");
        verify(processScenario).hasCompleted("Screen_CorrespondentsInvalid");
        verify(processScenario).hasCompleted("EndEvent_ComplaintCorrespondent");

        verify(bpmnService, times(2)).updatePrimaryCorrespondent(any(), any(), any());
        verify(bpmnService, times(2)).caseHasPrimaryCorrespondentType(any(), any());
    }

}
