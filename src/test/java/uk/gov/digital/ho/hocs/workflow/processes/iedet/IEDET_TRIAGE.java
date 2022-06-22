package uk.gov.digital.ho.hocs.workflow.processes.iedet;

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
@Deployment(resources = "processes/IEDET/IEDET_TRIAGE.bpmn")
public class IEDET_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario compServiceTriageProcess;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testTriageSendToCch(){
        when(compServiceTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "TriageAccept", "NoCch")));

        Scenario.run(compServiceTriageProcess).startByKey("IEDET_TRIAGE").execute();

        verify(compServiceTriageProcess).hasCompleted("Service_CreateCase");
        verify(compServiceTriageProcess).hasCompleted("EndEvent_IEDET_TRIAGE");
    }

    @Test
    public void testTriageNoConsideration(){
        when(compServiceTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "TriageAccept", "NoFurtherConsideration")));

        Scenario.run(compServiceTriageProcess).startByKey("IEDET_TRIAGE").execute();

        verify(compServiceTriageProcess).hasCompleted("EndEvent_IEDET_TRIAGE");
    }

    @Test
    public void testTriageResultCompleteYesThirdParty(){
        when(compServiceTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "TriageAccept", "false")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "TriageAccept", "YesThirdParty")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_BusArea"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        Scenario.run(compServiceTriageProcess).startByKey("IEDET_TRIAGE").execute();

        verify(compServiceTriageProcess, times(3)).hasCompleted("Screen_BusArea");
        verify(compServiceTriageProcess).hasCompleted("EndEvent_IEDET_TRIAGE");
    }

    @Test
    public void testTriageResultCompleteYesIedetCompliance(){
        when(compServiceTriageProcess.waitsAtUserTask("Validate_Accept"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "TriageAccept", "false")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "TriageAccept", "YesIEDETCompliance")));

        when(compServiceTriageProcess.waitsAtUserTask("Validate_BusArea"))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", false, "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        Scenario.run(compServiceTriageProcess).startByKey("IEDET_TRIAGE").execute();

        verify(compServiceTriageProcess, times(3)).hasCompleted("Screen_BusArea");
        verify(compServiceTriageProcess).hasCompleted("EndEvent_IEDET_TRIAGE");
    }

}
