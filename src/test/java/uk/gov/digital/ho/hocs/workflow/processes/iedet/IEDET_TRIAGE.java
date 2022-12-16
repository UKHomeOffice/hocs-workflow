package uk.gov.digital.ho.hocs.workflow.processes.iedet;

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
import uk.gov.digital.ho.hocs.workflow.api.bpmn.BusinessEventService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/IEDET/IEDET_TRIAGE.bpmn")
public class IEDET_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private BusinessEventService businessEventService;

    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
        Mocks.register("businessEventService", businessEventService);
    }

    @Test
    public void testHappyPath() {

        when(processScenario.waitsAtUserTask("Screen_ComplaintType")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask("Screen_ComplaintCategory"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "MinorMisconduct")));

        when(processScenario.waitsAtUserTask("Screen_ComplaintDetails_NotSerious"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask("Screen_Assign")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "TriageAssign", "Yes")));

        Scenario.run(processScenario).startByKey("IEDET_TRIAGE").execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario, times(3)).hasCompleted("Screen_ComplaintType");
        verify(processScenario, times(4)).hasCompleted("Screen_ComplaintCategory");
        verify(processScenario, times(4)).hasCompleted("Screen_ComplaintDetails_NotSerious");
        verify(processScenario, times(3)).hasCompleted("Screen_Assign");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }

    @Test
    public void testTriageTransferToCCH() {
        when(processScenario.waitsAtUserTask("Screen_ComplaintType")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask("Screen_ComplaintCategory"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "MinorMisconduct")));

        when(processScenario.waitsAtUserTask("Screen_ComplaintDetails_NotSerious"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask("Screen_Assign"))
            .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
            .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "TriageAssign", "CCH")));

        Scenario.run(processScenario).startByKey("IEDET_TRIAGE").execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario).hasCompleted("Screen_ComplaintType");
        verify(processScenario).hasCompleted("Screen_ComplaintCategory");
        verify(processScenario, times(2)).hasCompleted("Screen_ComplaintDetails_NotSerious");
        verify(processScenario, times(2)).hasCompleted("Screen_Assign");
        verify(processScenario).hasCompleted("Service_CreateTransferCaseNote");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }

    @Test
    public void testTriageEscalateToPSU() {
        when(processScenario.waitsAtUserTask("Screen_ComplaintType"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask("Screen_ComplaintCategory"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "SeriousMisconduct")));

        when(processScenario.waitsAtUserTask("Screen_ComplaintDetails_Serious"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey("IEDET_TRIAGE")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Triage");
        verify(processScenario).hasCompleted("Screen_ComplaintType");
        verify(processScenario, times(2)).hasCompleted("Screen_ComplaintCategory");
        verify(processScenario, times(3)).hasCompleted("Screen_ComplaintDetails_Serious");
        verify(processScenario).hasCompleted("EndEvent_Triage");
    }
}
