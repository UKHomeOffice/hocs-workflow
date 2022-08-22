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
@Deployment(resources = "processes/COMP/COMP_EXGRATIA_ESCALATE.bpmn")
public class COMP_EX_GRATIA_ESCALATE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario exGratiaEscalateProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testEscalateToTriage(){
        when(exGratiaEscalateProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", false)))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Pending")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Triage")));

        Scenario.run(exGratiaEscalateProcess).startByKey("COMP_EXGRATIA_ESCALATE").execute();

        verify(exGratiaEscalateProcess, times(3)).hasCompleted("Screen_Input");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_EXGRATIA_TRIAGE"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }

    @Test
    public void testEscalateToDraft(){
        when(exGratiaEscalateProcess.waitsAtUserTask("Validate_Input"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "CctEscalateResult", "Draft")));

        Scenario.run(exGratiaEscalateProcess).startByKey("COMP_EXGRATIA_ESCALATE").execute();

        verify(exGratiaEscalateProcess, times(1)).hasCompleted("Screen_Input");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("COMP_EXGRATIA_RESPONSE_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("Stage"));
    }
}
