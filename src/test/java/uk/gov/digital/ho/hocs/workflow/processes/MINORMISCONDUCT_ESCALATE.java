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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MINORMISCONDUCT_ESCALATE.bpmn")
public class MINORMISCONDUCT_ESCALATE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

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
    public void testEscalateToTriage(){
        when(minorMisconductEscalateProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Triage")));

        Scenario.run(minorMisconductEscalateProcess).startByKey("MINORMISCONDUCT_ESCALATE").execute();

        verify(minorMisconductEscalateProcess, times(3)).hasCompleted("Screen_Input");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MINORMISCONDUCT_TRIAGE"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }

    @Test
    public void testEscalateToDraft(){
        when(minorMisconductEscalateProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Draft")));

        Scenario.run(minorMisconductEscalateProcess).startByKey("MINORMISCONDUCT_ESCALATE").execute();

        verify(minorMisconductEscalateProcess, times(1)).hasCompleted("Screen_Input");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MINORMISCONDUCT_RESPONSE_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }
}
