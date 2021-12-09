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
@Deployment(resources = {"processes/TO_ENQUIRY_SUBJECT_REASON.bpmn"})
public class TO_ENQUIRY_SUBJECT_REASON {

    // COMMON GATEWAY OUTCOMES
    private static final String BACKWARD = "BACKWARD";
    private static final String FORWARD = "FORWARD";

    // USER AND SERVICE TASKS
    private static final String TO_ENQUIRY_SET_SUBJECT = "TO_ENQUIRY_SET_SUBJECT";
    private static final String TO_ENQUIRY_SET_REASON = "TO_ENQUIRY_SET_REASON";
    private static final String SET_ENQ_SUB_REASON = "Activity_1gwtvwm";
    private static final String CLEAR_TEMP_SUB_REASON = "Activity_032h2pq";
    private static final String CLEAR_TEMP_SUBJ = "Activity_05gltgp";

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
    public void shouldSetSubAndReason() {
        // GIVEN
        when(TOProcess.waitsAtUserTask(TO_ENQUIRY_SET_SUBJECT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", FORWARD)));

        when(TOProcess.waitsAtUserTask(TO_ENQUIRY_SET_REASON))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", FORWARD)));

        // WHEN
        Scenario.run(TOProcess)
                .startByKey("TO_ENQUIRY_SUBJECT_REASON")
                .execute();

        // THEN
        verify(TOProcess, times(1))
                .hasCompleted(TO_ENQUIRY_SET_SUBJECT);

        verify(TOProcess, times(1))
                .hasCompleted(TO_ENQUIRY_SET_REASON);

        verify(TOProcess, times(1))
                .hasCompleted(SET_ENQ_SUB_REASON);

        verify(TOProcess, times(1))
                .hasCompleted(CLEAR_TEMP_SUB_REASON);

        verify(TOProcess, times(0))
                .hasCompleted(CLEAR_TEMP_SUBJ);

    }

    @Test
    public void shouldClearSubjWhenBackwardsAtSetReason() {
        // GIVEN
        when(TOProcess.waitsAtUserTask(TO_ENQUIRY_SET_SUBJECT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", FORWARD)))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", BACKWARD)));

        when(TOProcess.waitsAtUserTask(TO_ENQUIRY_SET_REASON))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", BACKWARD)));

        // WHEN
        Scenario.run(TOProcess)
                .startByKey("TO_ENQUIRY_SUBJECT_REASON")
                .execute();

        // THEN
        verify(TOProcess, times(2))
                .hasCompleted(TO_ENQUIRY_SET_SUBJECT);

        verify(TOProcess, times(1))
                .hasCompleted(TO_ENQUIRY_SET_REASON);

        verify(TOProcess, times(0))
                .hasCompleted(SET_ENQ_SUB_REASON);

        verify(TOProcess, times(0))
                .hasCompleted(CLEAR_TEMP_SUB_REASON);

        verify(TOProcess, times(1))
                .hasCompleted(CLEAR_TEMP_SUBJ);

    }

    @Test
    public void shouldNotSetSubjOrReason() {
        // GIVEN
        when(TOProcess.waitsAtUserTask(TO_ENQUIRY_SET_SUBJECT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", BACKWARD)));


        // WHEN
        Scenario.run(TOProcess)
                .startByKey("TO_ENQUIRY_SUBJECT_REASON")
                .execute();

        // THEN
        verify(TOProcess, times(1))
                .hasCompleted(TO_ENQUIRY_SET_SUBJECT);

        verify(TOProcess, times(0))
                .hasCompleted(TO_ENQUIRY_SET_REASON);

        verify(TOProcess, times(0))
                .hasCompleted(SET_ENQ_SUB_REASON);

        verify(TOProcess, times(0))
                .hasCompleted(CLEAR_TEMP_SUB_REASON);

        verify(TOProcess, times(0))
                .hasCompleted(CLEAR_TEMP_SUBJ);

    }
}
