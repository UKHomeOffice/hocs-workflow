package uk.gov.digital.ho.hocs.workflow.processes.to;

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
@Deployment(resources = {"processes/TO_HOME_SEC_INTEREST.bpmn"})
public class TO_CHANGE_HOME_SEC_INTEREST {

    // COMMON GATEWAY OUTCOMES
    private static final String BACKWARD = "BACKWARD";
    private static final String FORWARD = "FORWARD";
    private static final String YES = "Yes";
    private static final String NO = "No";

    // USER AND SERVICE TASKS
    private static final String TO_CHANGE_HOME_SEC_INTEREST = "TO_CHANGE_HOME_SEC_INTEREST";
    private static final String SET_HOME_SEC_INTEREST_YES = "Activity_0ohoscn";
    private static final String SET_HOME_SEC_INTEREST_NO = "Activity_06napyy";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario TOProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void shouldChangeHomeSecInterestToYes() {

        when(TOProcess.waitsAtUserTask(TO_CHANGE_HOME_SEC_INTEREST)).thenReturn(
                task -> task.complete(
                        withVariables("HomeSecInterest", YES, "DIRECTION", FORWARD)
                )
        );

        Scenario.run(TOProcess)
                .startByKey("TO_HOME_SEC_INTEREST")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_CHANGE_HOME_SEC_INTEREST);

        verify(TOProcess, times(1))
                .hasCompleted(SET_HOME_SEC_INTEREST_YES);

        verify(TOProcess, times(0))
                .hasCompleted(SET_HOME_SEC_INTEREST_NO);

    }

    @Test
    public void shouldChangeHomeSecInterestToNo() {

        when(TOProcess.waitsAtUserTask(TO_CHANGE_HOME_SEC_INTEREST)).thenReturn(
                task -> task.complete(
                        withVariables("HomeSecInterest", NO, "DIRECTION", FORWARD)
                )
        );

        Scenario.run(TOProcess)
                .startByKey("TO_HOME_SEC_INTEREST")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_CHANGE_HOME_SEC_INTEREST);

        verify(TOProcess, times(0))
                .hasCompleted(SET_HOME_SEC_INTEREST_YES);

        verify(TOProcess, times(1))
                .hasCompleted(SET_HOME_SEC_INTEREST_NO);

    }
}
