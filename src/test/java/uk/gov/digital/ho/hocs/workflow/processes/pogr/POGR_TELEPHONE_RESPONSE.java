package uk.gov.digital.ho.hocs.workflow.processes.pogr;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/POGR/SHARED/POGR_TELEPHONE_RESPONSE.bpmn" })
public class POGR_TELEPHONE_RESPONSE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

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
        when(processScenario.waitsAtUserTask("Screen_TelephoneResponse")).thenReturn(task -> task.complete(
            withVariables("TelephoneResponse", "Yes", "TelephoneResponseCaseNote", "Test", "DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startByKey("POGR_TELEPHONE_RESPONSE").execute();

        verify(processScenario).hasCompleted("StartEvent_TelephoneResponse");
        verify(processScenario).hasCompleted("Screen_TelephoneResponse");
        verify(processScenario).hasCompleted("Service_TelephoneCreateCaseNote");
        verify(processScenario).hasCompleted("EndEvent_TelephoneResponse");

        verify(bpmnService).createCaseNote(any(), eq("Test"), eq("PHONECALL"));
    }

    @Test
    public void testNonTelephone() {
        when(processScenario.waitsAtUserTask("Screen_TelephoneResponse")).thenReturn(task -> task.complete(
            withVariables("TelephoneResponse", "No", "TelephoneResponseCaseNote", "Test", "DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startByKey("POGR_TELEPHONE_RESPONSE").execute();

        verify(processScenario).hasCompleted("StartEvent_TelephoneResponse");
        verify(processScenario).hasCompleted("Screen_TelephoneResponse");
        verify(processScenario).hasCompleted("Service_TelephoneCreateCaseNote");
        verify(processScenario).hasCompleted("EndEvent_TelephoneResponse");

        verify(bpmnService).createCaseNote(any(), eq("Test"), eq("PHONECALL"));
    }

    @Test
    public void testBackTelephone() {
        when(processScenario.waitsAtUserTask("Screen_TelephoneResponse")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD")));

        Scenario.run(processScenario).startByKey("POGR_TELEPHONE_RESPONSE").execute();

        verify(processScenario).hasCompleted("StartEvent_TelephoneResponse");
        verify(processScenario).hasCompleted("Screen_TelephoneResponse");
        verify(processScenario, times(0)).hasCompleted("Service_TelephoneCreateCaseNote");
        verify(processScenario).hasCompleted("EndEvent_TelephoneResponse");

        verify(bpmnService, times(0)).createCaseNote(any(), any(), any());
    }

}
