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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/POGR/POGR_GRO_DRAFT.bpmn"
})
public class POGR_GRO_DRAFT {

    public static final String REJECT_INVESTIGATION = "Activity_0jgtbme";
    public static final String SAVE_REJECTION_NOTE = "Activity_136hbub";

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
        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DraftOutcome", "")
                .thenReturn("DraftOutcome", "QA")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_GroDraft");
        verify(processScenario, times(2)).hasCompleted("CallActivity_DraftInput");
        verify(processScenario).hasCompleted("EndEvent_GroDraft");
    }

    @Test
    public void testTelephoneResponse() {
        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DraftOutcome", "TelephoneResponse")
                .deploy(rule);

        whenAtCallActivity("POGR_TELEPHONE_RESPONSE")
                .thenReturn("TelephoneResponse", "", "DIRECTION", "Backward")
                .thenReturn("TelephoneResponse", "")
                .thenReturn("TelephoneResponse", "Yes")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_GroDraft");
        verify(processScenario).hasCompleted("CallActivity_DraftInput");
        verify(processScenario, times(3)).hasCompleted("CallActivity_TelephoneResponse");
        verify(processScenario).hasCompleted("EndEvent_GroDraft");
    }

    @Test
    public void testNotTelephoneResponse() {
        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DraftOutcome", "TelephoneResponse")
                .thenReturn("DraftOutcome", "QA")
                .deploy(rule);

        whenAtCallActivity("POGR_TELEPHONE_RESPONSE")
                .thenReturn("TelephoneResponse", "No")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_GroDraft");
        verify(processScenario, times(2)).hasCompleted("CallActivity_DraftInput");
        verify(processScenario).hasCompleted("CallActivity_TelephoneResponse");
        verify(processScenario).hasCompleted("EndEvent_GroDraft");
    }

    @Test
    public void testRejectionPath() {
        whenAtCallActivity("POGR_GRO_PRIORITY_CHANGE_SCREEN")
                .thenReturn("DraftOutcome", "ReturnInvestigation")
                .thenReturn("DraftOutcome", "ReturnInvestigation")
                .deploy(rule);

        when(processScenario.waitsAtUserTask(REJECT_INVESTIGATION))
                .thenReturn(task -> task.complete(withVariables("DIRECTION","UNKNOWN")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION","BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION","FORWARD")));

        Scenario.run(processScenario)
                .startByKey("POGR_GRO_DRAFT")
                .execute();

        verify(processScenario).hasCompleted("StartEvent_GroDraft");
        verify(processScenario, times(2)).hasCompleted("CallActivity_DraftInput");
        verify(processScenario, times(3)).hasCompleted(REJECT_INVESTIGATION);
        verify(processScenario, times(1)).hasCompleted(SAVE_REJECTION_NOTE);
        verify(processScenario).hasCompleted("EndEvent_GroDraft");
    }



}
