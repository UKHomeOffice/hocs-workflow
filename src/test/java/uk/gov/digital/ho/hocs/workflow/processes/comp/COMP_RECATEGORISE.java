package uk.gov.digital.ho.hocs.workflow.processes.comp;

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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/COMP/COMP_RECATEGORISE.bpmn")
public class COMP_RECATEGORISE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.93).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario compRecategoriseProcess;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);

        when(compRecategoriseProcess.waitsAtUserTask("Screen_ComplaintType")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Service")));
    }

    @Test
    public void happyPath() {
        Scenario.run(compRecategoriseProcess).startByKey("COMP_RECATEGORISE").execute();

        verify(compRecategoriseProcess).hasCompleted("Screen_ComplaintType");
    }

    @Test
    public void defaultsBackToComplaintTypeIfNotMovingForward() {
        when(compRecategoriseProcess.waitsAtUserTask("Screen_ComplaintType")).thenReturn(
            task -> task.complete(withVariables("DIRECTION", ""))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD", "CompType", "Service")));

        Scenario.run(compRecategoriseProcess).startByKey("COMP_RECATEGORISE").execute();

        verify(compRecategoriseProcess, times(2)).hasCompleted("Screen_ComplaintType");
    }
}
