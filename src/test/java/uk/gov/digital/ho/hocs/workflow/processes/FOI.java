package uk.gov.digital.ho.hocs.workflow.processes;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/STAGE.bpmn",
        "processes/FOI.bpmn",
        "processes/FOI_DATA_INPUT.bpmn",
        "processes/FOI_ALLOCATION.bpmn",
        "processes/FOI_ACCEPTANCE.bpmn"
})
public class FOI {

    public static final String DATA_INPUT_ACTIVITY = "Activity_0jtkbij";
    public static final String ALLOCATION_ACTIVITY = "Activity_16l1q7b";
    public static final String COMPLETE_CASE_ACTIVITY = "Activity_1d2ue6g";
    public static final String ACCEPTANCE_ACTIVITY = "ACCEPTANCE";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario FOIProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void happyPath() {

        whenAtCallActivity("FOI_DATA_INPUT")
                .deploy(rule);

        whenAtCallActivity("FOI_ALLOCATION")
            .deploy(rule);

        whenAtCallActivity("FOI_ACCEPTANCE")
                .thenReturn("AcceptCase", "Y")
                .deploy(rule);


        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(DATA_INPUT_ACTIVITY);

        verify(FOIProcess, times(1))
            .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE_ACTIVITY);

        verify(bpmnService).completeCase(any());

    }

    @Test
    public void caseRejectedAtAllocation() {

        whenAtCallActivity("FOI_DATA_INPUT")
                .deploy(rule);

        whenAtCallActivity("FOI_ALLOCATION")
                .deploy(rule);

        whenAtCallActivity("FOI_ACCEPTANCE")
                .thenReturn("AcceptCase", "N")
                .thenReturn("AcceptCase", "Y")
                .deploy(rule);


        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(DATA_INPUT_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE_ACTIVITY);

        verify(bpmnService).completeCase(any());

    }
}
