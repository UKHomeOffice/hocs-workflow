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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/DCU_MIN_MINISTER_SIGN_OFF.bpmn")
public class DCU_MIN_MinisterSignOff {

    public static final String APPROVE_MINISTER_SIGN_OFF = "UserTask_0eagz4p";
    public static final String DCU_MIN_MINISTER_SIGN_OFF_END = "DCU_MIN_MINISTER_SIGN_OFF_END";
    public static final String VALIDATE_REJECTION_NOTE = "UserTask_04hin8c";
    public static final String DCU_MIN_MINISTER_SIGN_OFF = "DCU_MIN_MINISTER_SIGN_OFF";
    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.88)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario mpamCreationProcess;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void acceptSignOffScenario() {

        when(mpamCreationProcess.waitsAtUserTask(APPROVE_MINISTER_SIGN_OFF))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "ACCEPT")));

        Scenario.run(mpamCreationProcess)
                .startByKey(DCU_MIN_MINISTER_SIGN_OFF)
                .execute();

        verify(mpamCreationProcess)
                .hasFinished(DCU_MIN_MINISTER_SIGN_OFF_END);

        verify(mpamCreationProcess, never()).hasFinished(VALIDATE_REJECTION_NOTE);

    }

    @Test
    public void rejectSignOffScenario() {

        when(mpamCreationProcess.waitsAtUserTask(APPROVE_MINISTER_SIGN_OFF))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "REJECT")));

        when(mpamCreationProcess.waitsAtUserTask(DCU_MIN_MinisterSignOff.VALIDATE_REJECTION_NOTE))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));


        Scenario.run(mpamCreationProcess)
                .startByKey("DCU_MIN_MINISTER_SIGN_OFF")
                .execute();

        verify(mpamCreationProcess)
                .hasFinished(VALIDATE_REJECTION_NOTE);

        verify(mpamCreationProcess)
                .hasFinished(DCU_MIN_MINISTER_SIGN_OFF_END);
    }
}
