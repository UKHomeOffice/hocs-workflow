package uk.gov.digital.ho.hocs.workflow.processes.comp;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/COMP/COMP_PSU_TRIAGE.bpmn"})
public class COMP_PSU_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

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
        when(processScenario.waitsAtUserTask("Screen_PSUComplaints")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "", "PsuTriageOutcome", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD","PsuTriageOutcome", "Accept")));

        when(processScenario.waitsAtUserTask("Screen_PSUComplaintCategory")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("COMP_PSU_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario, times(3)).hasCompleted("Screen_PSUComplaints");
        verify(processScenario, times(3)).hasCompleted("Screen_PSUComplaintCategory");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }

    @Test
    public void testCloseCase() {
        when(processScenario.waitsAtUserTask("Screen_PSUComplaints")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "PsuTriageOutcome", "CloseCase")));

        Scenario.run(processScenario)
            .startByKey("COMP_PSU_TRIAGE")
            .execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario, times(1)).hasCompleted("Screen_PSUComplaints");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }

    @Test
    public void testReturnCase() {
        when(processScenario.waitsAtUserTask("Screen_PSUComplaints")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "PsuTriageOutcome", "ReturnCase")));

        Scenario.run(processScenario)
            .startByKey("COMP_PSU_TRIAGE")
            .execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario, times(1)).hasCompleted("Screen_PSUComplaints");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }

}
