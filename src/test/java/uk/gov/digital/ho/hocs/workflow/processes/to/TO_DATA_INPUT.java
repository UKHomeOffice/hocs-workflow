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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/TO/TO_DATA_INPUT.bpmn"})
public class TO_DATA_INPUT {

    // ACTIVITIES and USER TASKS
    private static final String CAPTURE_BUSINESS_AREA = "Activity_04japfx";
    private static final String ALLOCATE_TO_UKVI_TEAM = "Activity_10xdhy1";
    private static final String ALLOCATE_TO_BF_TEAM   = "Activity_1u2gne9";
    private static final String ALLOCATE_TO_HMPO_TEAM = "Activity_08iee1e";
    private static final String CAPTURE_CORRESPONDENT_DETAILS = "Activity_08vy6zg";
    private static final String SAVE_CORRESPONDENT_DETAILS = "SAVE_CORRESPONDENT_DETAILS";
    private static final String ADD_RECIPIENT = "Activity_03q1s1y";

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
    public void UKVITeamCaseHappy() {

        when(TOProcess.waitsAtUserTask(CAPTURE_BUSINESS_AREA))
                .thenReturn(task -> task.complete(withVariables(
                        "BusinessArea", "UKVI")
                ));

        when(TOProcess.waitsAtUserTask(CAPTURE_CORRESPONDENT_DETAILS))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD")
                ));

        when(TOProcess.waitsAtUserTask(ADD_RECIPIENT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD","RecipientAdded", "YES")));

        Scenario.run(TOProcess)
                .startByKey("TO_DATA_INPUT")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(CAPTURE_BUSINESS_AREA);

        verify(TOProcess, times(1))
                .hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(1))
                .hasCompleted(CAPTURE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(1))
                .hasCompleted(SAVE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(1))
                .hasCompleted(ADD_RECIPIENT);

    }

    @Test
    public void UKVITeamCaseHappyNoRecipient() {

        when(TOProcess.waitsAtUserTask(CAPTURE_BUSINESS_AREA))
                .thenReturn(task -> task.complete(withVariables(
                        "BusinessArea", "UKVI")
                ));

        when(TOProcess.waitsAtUserTask(CAPTURE_CORRESPONDENT_DETAILS))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD")
                ));

        when(TOProcess.waitsAtUserTask(ADD_RECIPIENT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD","RecipientAdded", "NO")));

        Scenario.run(TOProcess)
                .startByKey("TO_DATA_INPUT")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(CAPTURE_BUSINESS_AREA);

        verify(TOProcess, times(1))
                .hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(1))
                .hasCompleted(CAPTURE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(1))
                .hasCompleted(SAVE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(1))
                .hasCompleted(ADD_RECIPIENT);

    }


    @Test
    public void BFTeamCaseHappy() {

        when(TOProcess.waitsAtUserTask(CAPTURE_BUSINESS_AREA))
                .thenReturn(task -> task.complete(withVariables(
                        "BusinessArea", "BF")
                ));

        when(TOProcess.waitsAtUserTask(CAPTURE_CORRESPONDENT_DETAILS))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD")
                ));

        when(TOProcess.waitsAtUserTask(ADD_RECIPIENT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD","RecipientAdded", "YES")));


        Scenario.run(TOProcess)
                .startByKey("TO_DATA_INPUT")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(CAPTURE_BUSINESS_AREA);

        verify(TOProcess, times(1))
                .hasCompleted(SAVE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(1))
                .hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(1))
                .hasCompleted(CAPTURE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(1))
                .hasCompleted(ADD_RECIPIENT);

    }

    @Test
    public void HMPOTeamCaseHappy() {

        when(TOProcess.waitsAtUserTask(CAPTURE_BUSINESS_AREA))
                .thenReturn(task -> task.complete(withVariables(
                        "BusinessArea", "HMPO")
                ));

        when(TOProcess.waitsAtUserTask(CAPTURE_CORRESPONDENT_DETAILS))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD")
                ));

        when(TOProcess.waitsAtUserTask(ADD_RECIPIENT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD","RecipientAdded", "YES")));

        Scenario.run(TOProcess)
                .startByKey("TO_DATA_INPUT")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(CAPTURE_BUSINESS_AREA);

        verify(TOProcess, times(1))
                .hasCompleted(SAVE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(1))
                .hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(1))
                .hasCompleted(CAPTURE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(1))
                .hasCompleted(SAVE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(1))
                .hasCompleted(ADD_RECIPIENT);

    }

    @Test
    public void UKVITeamCaseAllBackLinks() {

        when(TOProcess.waitsAtUserTask(CAPTURE_BUSINESS_AREA))
                .thenReturn(task -> task.complete(withVariables(
                        "BusinessArea", "UKVI")
                ));

        when(TOProcess.waitsAtUserTask(CAPTURE_CORRESPONDENT_DETAILS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(TOProcess.waitsAtUserTask(ADD_RECIPIENT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD","RecipientAdded", "YES")));

        Scenario.run(TOProcess)
                .startByKey("TO_DATA_INPUT")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(CAPTURE_BUSINESS_AREA);

        verify(TOProcess, times(2))
                .hasCompleted(ALLOCATE_TO_UKVI_TEAM);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_BF_TEAM);

        verify(TOProcess, times(0))
                .hasCompleted(ALLOCATE_TO_HMPO_TEAM);

        verify(TOProcess, times(3))
                .hasCompleted(CAPTURE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(2))
                .hasCompleted(SAVE_CORRESPONDENT_DETAILS);

        verify(TOProcess, times(2))
                .hasCompleted(ADD_RECIPIENT);

    }


}
