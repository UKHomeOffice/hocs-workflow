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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP_MINORMISCONDUCT_RESPONSE_DRAFT.bpmn")
public class COMP_MINORMISCONDUCT_DRAFT {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario minorMisconductDraftProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testDefaultPassThrough(){
        when(minorMisconductDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "CctDraftResult", "Ignore")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "Ignore")));

        Scenario.run(minorMisconductDraftProcess).startByKey("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").execute();

        verify(minorMisconductDraftProcess, times(2)).hasCompleted("Screen_Input");
    }

    @Test
    public void testSendRoute(){
        when(minorMisconductDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "Send")));

        Scenario.run(minorMisconductDraftProcess).startByKey("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").execute();

        verify(minorMisconductDraftProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Send");
    }

    @Test
    public void testQARoute(){
        when(minorMisconductDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "QA")));

        Scenario.run(minorMisconductDraftProcess).startByKey("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").execute();

        verify(minorMisconductDraftProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_QA");
    }

    @Test
    public void testEscalateRoute(){
        when(minorMisconductDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "Escalate")));
        when(minorMisconductDraftProcess.waitsAtUserTask("Validate_Escalate"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "valid", "false")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "valid", "true")));

        Scenario.run(minorMisconductDraftProcess).startByKey("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").execute();

        verify(minorMisconductDraftProcess, times(3)).hasCompleted("Screen_Escalate");
        verify(minorMisconductDraftProcess).hasCompleted("Service_UpdateAllocationNote_Escalate");
        verify(minorMisconductDraftProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Escalate");
    }

    @Test
    public void testReturnRoute(){
        when(minorMisconductDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "Return")));
        when(minorMisconductDraftProcess.waitsAtUserTask("Validate_Return"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "valid", "false")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "valid", "true")));

        Scenario.run(minorMisconductDraftProcess).startByKey("COMP_MINORMISCONDUCT_RESPONSE_DRAFT").execute();

        verify(minorMisconductDraftProcess, times(3)).hasCompleted("Screen_Return");
        verify(minorMisconductDraftProcess).hasCompleted("Service_UpdateAllocationNote_Reject");
        verify(minorMisconductDraftProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
    }

}
