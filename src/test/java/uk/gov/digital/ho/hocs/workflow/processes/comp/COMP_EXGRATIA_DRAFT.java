package uk.gov.digital.ho.hocs.workflow.processes.comp;

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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP/COMP_EXGRATIA_RESPONSE_DRAFT.bpmn")
public class COMP_EXGRATIA_DRAFT {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario exGratiaDraftProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testDefaultPassThrough(){
        when(exGratiaDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "CctDraftResult", "Ignore")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "Ignore")));

        Scenario.run(exGratiaDraftProcess).startByKey("COMP_EXGRATIA_RESPONSE_DRAFT").execute();

        verify(exGratiaDraftProcess, times(2)).hasCompleted("Screen_Input");
    }

    @Test
    public void testSendRoute(){
        when(exGratiaDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "Send")));

        Scenario.run(exGratiaDraftProcess).startByKey("COMP_EXGRATIA_RESPONSE_DRAFT").execute();

        verify(exGratiaDraftProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Send");
    }

    @Test
    public void testQARoute(){
        when(exGratiaDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "QA")));

        Scenario.run(exGratiaDraftProcess).startByKey("COMP_EXGRATIA_RESPONSE_DRAFT").execute();

        verify(exGratiaDraftProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_QA");
    }

    @Test
    public void testEscalateRoute(){
        when(exGratiaDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "Escalate")));
        when(exGratiaDraftProcess.waitsAtUserTask("Validate_Escalate"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "valid", "false")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "valid", "true")));

        Scenario.run(exGratiaDraftProcess).startByKey("COMP_EXGRATIA_RESPONSE_DRAFT").execute();

        verify(exGratiaDraftProcess, times(3)).hasCompleted("Screen_Escalate");
        verify(exGratiaDraftProcess).hasCompleted("Service_UpdateAllocationNote_Escalate");
        verify(exGratiaDraftProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Escalate");
    }

    @Test
    public void testReturnRoute(){
        when(exGratiaDraftProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctDraftResult", "Return")));
        when(exGratiaDraftProcess.waitsAtUserTask("Validate_Return"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "valid", "false")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "valid", "true")));

        Scenario.run(exGratiaDraftProcess).startByKey("COMP_EXGRATIA_RESPONSE_DRAFT").execute();

        verify(exGratiaDraftProcess, times(3)).hasCompleted("Screen_Return");
        verify(exGratiaDraftProcess).hasCompleted("Service_UpdateAllocationNote_Reject");
        verify(exGratiaDraftProcess).hasCompleted("Service_UpdateTeamByStageAndTexts_Draft");
    }

}
