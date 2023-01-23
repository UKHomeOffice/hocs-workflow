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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP/COMP_MINORMISCONDUCT_ESCALATE.bpmn")
public class COMP_MINORMISCONDUCT_ESCALATE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario minorMisconductEscalateProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testEscalateToTriage() {
        when(minorMisconductEscalateProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", false))).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Pending"))).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Triage")));

        Scenario.run(minorMisconductEscalateProcess).startByKey("COMP_MINORMISCONDUCT_ESCALATE").execute();

        verify(minorMisconductEscalateProcess, times(3)).hasCompleted("Screen_Input");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_MINORMISCONDUCT_TRIAGE"),
            eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }

    @Test
    public void testEscalateToDraft() {
        when(minorMisconductEscalateProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Draft")));

        Scenario.run(minorMisconductEscalateProcess).startByKey("COMP_MINORMISCONDUCT_ESCALATE").execute();

        verify(minorMisconductEscalateProcess, times(1)).hasCompleted("Screen_Input");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_MINORMISCONDUCT_RESPONSE_DRAFT"),
            eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }

    @Test
    public void testEscalateToPSU() {
        when(minorMisconductEscalateProcess.waitsAtUserTask("Validate_Input")).thenReturn(
            task -> task.complete(withVariables("valid", true, "CctEscalateResult", "PSU")));

        when(minorMisconductEscalateProcess.waitsAtUserTask("Activity_ScreenCategorySerious")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(minorMisconductEscalateProcess).startByKey("COMP_MINORMISCONDUCT_ESCALATE").execute();

        verify(minorMisconductEscalateProcess, times(2)).hasCompleted("Screen_Input");
        verify(minorMisconductEscalateProcess, times(3)).hasCompleted("Activity_ScreenCategorySerious");
    }
}
