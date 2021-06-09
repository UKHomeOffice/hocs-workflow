package uk.gov.digital.ho.hocs.workflow.processes.mts;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.mockito.ProcessExpressions;
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
@Deployment(resources = "processes/MTS_DATA_INPUT.bpmn")
public class MTS_DATA_INPUT {

    public static final String SET_PRIMARY_CORRESPONDENT_SCREEN = "ServiceTask_1j8d7km";
    public static final String SET_PRIMARY_CORRESPONDENT_USER_TASK = "UserTask_1bx67tn";

    public static final String MTS_DI_DETAILS_SCREEN = "ServiceTask_1j88374";
    public static final String MTS_DI_DETAILS_USER_TASK = "UserTask_0uu8g19";

    public static final String UPDATE_PRIMARY_CORRESPONDENT_SERVICE_TASK = "ServiceTask_1y5iqza";

    public static final String END_EVENT = "EndEvent_168q5al";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .build();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario mtsProcess;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);

        when(mtsProcess.waitsAtUserTask(SET_PRIMARY_CORRESPONDENT_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true)));

        when(mtsProcess.waitsAtUserTask(MTS_DI_DETAILS_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));
    }

    @Test
    public void happyPath() {
        Scenario.run(mtsProcess)
                .startByKey("MTS_DATA_INPUT")
                .execute();

        verify(mtsProcess)
                .hasCompleted(SET_PRIMARY_CORRESPONDENT_SCREEN);
        verify(mtsProcess)
                .hasCompleted(UPDATE_PRIMARY_CORRESPONDENT_SERVICE_TASK);
        verify(mtsProcess)
                .hasCompleted(MTS_DI_DETAILS_SCREEN);

        verify(mtsProcess).hasFinished(END_EVENT);
    }

    @Test
    public void whenInvalidPrimaryCorrespondentScreen_RetriesScreen() {
        when(mtsProcess.waitsAtUserTask(SET_PRIMARY_CORRESPONDENT_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true)));

        Scenario.run(mtsProcess)
                .startByKey("MTS_DATA_INPUT")
                .execute();

        verify(mtsProcess, times(2))
                .hasCompleted(SET_PRIMARY_CORRESPONDENT_SCREEN);
        verify(mtsProcess)
                .hasCompleted(UPDATE_PRIMARY_CORRESPONDENT_SERVICE_TASK);
        verify(mtsProcess)
                .hasCompleted(MTS_DI_DETAILS_SCREEN);

        verify(mtsProcess).hasFinished(END_EVENT);
    }

    @Test
    public void whenInvalidDateInputScreen_RetriesScreen() {
        when(mtsProcess.waitsAtUserTask(MTS_DI_DETAILS_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false,
                        "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(mtsProcess)
                .startByKey("MTS_DATA_INPUT")
                .execute();

        verify(mtsProcess)
                .hasCompleted(SET_PRIMARY_CORRESPONDENT_SCREEN);
        verify(mtsProcess)
                .hasCompleted(UPDATE_PRIMARY_CORRESPONDENT_SERVICE_TASK);
        verify(mtsProcess, times(2))
                .hasCompleted(MTS_DI_DETAILS_SCREEN);

        verify(mtsProcess).hasFinished(END_EVENT);
    }

    @Test
    public void whenBackwardDateInputScreen_GoesBack() {
        when(mtsProcess.waitsAtUserTask(MTS_DI_DETAILS_USER_TASK))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD")));

        Scenario.run(mtsProcess)
                .startByKey("MTS_DATA_INPUT")
                .execute();

        verify(mtsProcess, times(2))
                .hasCompleted(SET_PRIMARY_CORRESPONDENT_SCREEN);
        verify(mtsProcess, times(2))
                .hasCompleted(UPDATE_PRIMARY_CORRESPONDENT_SERVICE_TASK);
        verify(mtsProcess, times(2))
                .hasCompleted(MTS_DI_DETAILS_SCREEN);

        verify(mtsProcess).hasFinished(END_EVENT);
    }
}
