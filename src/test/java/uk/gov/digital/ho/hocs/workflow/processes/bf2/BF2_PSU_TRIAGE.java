package uk.gov.digital.ho.hocs.workflow.processes.bf2;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
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
        "processes/BF2/BF2_PSU_TRIAGE.bpmn"})
public class BF2_PSU_TRIAGE {

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
            task -> task.complete(withVariables("PsuTriageOutcome", ""))).thenReturn(
            task -> task.complete(withVariables("PsuTriageOutcome", "Accept")));

        when(processScenario.waitsAtUserTask("Screen_PSUComplaintCategory")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
            .startByKey("BF2_PSU_TRIAGE")
            .execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario, times(3)).hasCompleted("Screen_PSUComplaints");
        verify(processScenario, times(3)).hasCompleted("Screen_PSUComplaintCategory");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }

    @Test
    public void testSetComplaintTypeAsCloseCase() {
        when(processScenario.waitsAtUserTask("Screen_PSUComplaints")).thenReturn(
            task -> task.complete(withVariables(
                "PsuTriageOutcome", "CloseCase")));

        Scenario.run(processScenario)
            .startByKey("BF2_PSU_TRIAGE")
            .execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario).hasCompleted("Screen_PSUComplaints");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }

    @Test
    public void testSetComplaintTypeAsReturnCase() {
        when(processScenario.waitsAtUserTask("Screen_PSUComplaints")).thenReturn(
            task -> task.complete(withVariables(
                "PsuTriageOutcome", "ReturnCase")));

        Scenario.run(processScenario)
            .startByKey("BF2_PSU_TRIAGE")
            .execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario).hasCompleted("Screen_PSUComplaints");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }
}
