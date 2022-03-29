package uk.gov.digital.ho.hocs.workflow.processes.pogr;

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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/POGR/POGR_GRO.bpmn",
        "processes/STAGE.bpmn" })
public class POGR_GRO {

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
        whenAtCallActivity("POGR_GRO_TRIAGE")
                .thenReturn("TriageAccept", "")
                .thenReturn("TriageAccept", "Accept")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO");
        verify(processScenario, times(2)).hasCompleted("CallActivity_PogrGroTriage");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO");
    }

    @Test
    public void testTriageCloseCase() {
        whenAtCallActivity("POGR_GRO_TRIAGE")
                .thenReturn("CloseCase", "true")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_GRO");
        verify(processScenario).hasCompleted("CallActivity_PogrGroTriage");
        verify(processScenario).hasCompleted("EndEvent_POGR_GRO");
    }

}
