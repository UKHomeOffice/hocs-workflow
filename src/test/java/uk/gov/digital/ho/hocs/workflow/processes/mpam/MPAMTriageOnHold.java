package uk.gov.digital.ho.hocs.workflow.processes.mpam;

import org.camunda.bpm.engine.test.Deployment;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM/MPAM_TRIAGE_ON_HOLD.bpmn")
public class MPAMTriageOnHold {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        0.83).build();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario mpamTriageOnHoldScenario;

    @Before
    public void defaultScenario() {

        Mocks.register("bpmnService", bpmnService);

        when(mpamTriageOnHoldScenario.waitsAtUserTask("UserTask_0jxw8et")).thenReturn(task -> task.complete(
            withVariables("valid", true, "TriageOnHoldOutcome", "PutOnCampaign", "DIRECTION", "FORWARD")));
        when(mpamTriageOnHoldScenario.waitsAtUserTask("UserTask_1ql7p2r")).thenReturn(
            task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));
    }

    @Test
    public void happyPath() {

        Scenario.run(mpamTriageOnHoldScenario).startByKey("MPAM_TRIAGE_ON_HOLD").execute();

        verify(mpamTriageOnHoldScenario).hasCompleted("ServiceTask_1smdf47");
        verify(mpamTriageOnHoldScenario).hasCompleted("ServiceTask_18u5rz3");
        verify(mpamTriageOnHoldScenario).hasCompleted("ServiceTask_0ms9mqv");
        verify(mpamTriageOnHoldScenario).hasFinished("EndEvent_1golwf2");

        verify(bpmnService).blankCaseValues(any(), any(), any());

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), any(), any(), any(), any(), any());

    }

}
