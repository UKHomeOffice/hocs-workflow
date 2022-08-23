package uk.gov.digital.ho.hocs.workflow.processes.pogr2;

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
@Deployment(resources = {
        "processes/POGR2/POGR2_HMPO_INVESTIGATION.bpmn"
})
public class POGR2_HMPO_INVESTIGATION {

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
        when(processScenario.waitsAtUserTask("Screen_InvestigationAcceptCase"))
                .thenReturn(task -> task.complete(withVariables("InvestigationAccept", "")))
                .thenReturn(task -> task.complete(withVariables("InvestigationAccept", "Accept")));

        Scenario.run(processScenario)
                .startByKey("POGR2_HMPO_INVESTIGATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_HMPO_INVESTIGATION");
        verify(processScenario, times(2)).hasCompleted("Screen_InvestigationAcceptCase");
        verify(processScenario).hasCompleted("EndEvent_POGR_HMPO_INVESTIGATION");
    }

    @Test
    public void testRejectCase() {
        when(processScenario.waitsAtUserTask("Screen_InvestigationAcceptCase"))
                .thenReturn(task -> task.complete(withVariables("InvestigationAccept", "Reject")));

        Scenario.run(processScenario)
                .startByKey("POGR2_HMPO_INVESTIGATION")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_POGR_HMPO_INVESTIGATION");
        verify(processScenario).hasCompleted("Screen_InvestigationAcceptCase");
        verify(processScenario).hasCompleted("Activity_RejectCaseNote");
        verify(processScenario).hasCompleted("EndEvent_POGR_HMPO_INVESTIGATION");
    }

}
