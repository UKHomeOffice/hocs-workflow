package uk.gov.digital.ho.hocs.workflow.processes.bf2;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/BF2/BF2_REGISTRATION.bpmn")
public class BF2_REGISTRATION {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario process;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testHappyPath(){
        when(process.waitsAtUserTask("Validate_Correspondents"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true)));

        when(bpmnService.caseHasPrimaryCorrespondentType(any(), eq("COMPLAINANT"))).thenReturn(true);

        when(process.waitsAtUserTask("Validate_Complainant"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Complaint_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD", "CompType", "Service")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD", "CompType", "Service")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompType", "Service")));

        Scenario.run(process).startByKey("BF2_REGISTRATION").execute();

        verify(bpmnService, times(2)).updatePrimaryCorrespondent(any(), any(), any());
        verify(bpmnService).updateValue(any(), any(), eq("Stage"), eq("Stage2"));
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("BF2_TRIAGE"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
        verify(process).hasCompleted("EndEvent_BF2_Registration");
    }

    @Test
    public void testNoPrimaryCorrespondents(){
        when(process.waitsAtUserTask("Validate_Correspondents"))
                .thenReturn(task -> task.complete(withVariables("valid", true)));

        when(bpmnService.caseHasPrimaryCorrespondentType(any(), eq("COMPLAINANT")))
                .thenReturn(false)
                .thenReturn(true);

        when(process.waitsAtUserTask("Validate_Invalid_Correspondents"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true)));

        when(process.waitsAtUserTask("Validate_Complainant"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        when(process.waitsAtUserTask("Validate_Complaint_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CompType", "Service")));

        Scenario.run(process).startByKey("BF2_REGISTRATION").execute();

        verify(process, times(2)).hasCompleted("hasPrimaryCorrespondents");
        verify(process, times(2)).hasCompleted("Invalid_Correspondents");
        verify(bpmnService).updateValue(any(), any(), eq("Stage"), eq("Stage2"));
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("BF2_TRIAGE"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
        verify(process).hasCompleted("EndEvent_BF2_Registration");
    }
}
