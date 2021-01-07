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
    public static final String VALIDATE_NOT_APPLICABLE = "UserTask_1j77wfm";
    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.86)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario dcuMinSignOffProcess;

    @Before
    public void defaultScenario() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void acceptSignOffScenario() {

        when(dcuMinSignOffProcess.waitsAtUserTask(APPROVE_MINISTER_SIGN_OFF))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "ACCEPT")));

        Scenario.run(dcuMinSignOffProcess)
                .startByKey(DCU_MIN_MINISTER_SIGN_OFF)
                .execute();

        verify(dcuMinSignOffProcess)
                .hasFinished(DCU_MIN_MINISTER_SIGN_OFF_END);

        verify(dcuMinSignOffProcess, never()).hasFinished(VALIDATE_REJECTION_NOTE);

    }

    @Test
    public void rejectSignOffScenario() {

        when(dcuMinSignOffProcess.waitsAtUserTask(APPROVE_MINISTER_SIGN_OFF))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "REJECT")));

        when(dcuMinSignOffProcess.waitsAtUserTask(DCU_MIN_MinisterSignOff.VALIDATE_REJECTION_NOTE))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));


        Scenario.run(dcuMinSignOffProcess)
                .startByKey("DCU_MIN_MINISTER_SIGN_OFF")
                .execute();

        verify(dcuMinSignOffProcess)
                .hasFinished(VALIDATE_REJECTION_NOTE);

        verify(dcuMinSignOffProcess)
                .hasFinished(DCU_MIN_MINISTER_SIGN_OFF_END);

        verify(dcuMinSignOffProcess, never()).hasFinished(VALIDATE_NOT_APPLICABLE);

    }

    @Test
    public void notApplicableScenario() {

        when(dcuMinSignOffProcess.waitsAtUserTask(APPROVE_MINISTER_SIGN_OFF))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "NOT_APPLICABLE")));

        when(dcuMinSignOffProcess.waitsAtUserTask(DCU_MIN_MinisterSignOff.VALIDATE_NOT_APPLICABLE))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        Scenario.run(dcuMinSignOffProcess)
                .startByKey("DCU_MIN_MINISTER_SIGN_OFF")
                .execute();

        verify(dcuMinSignOffProcess)
                .hasFinished(VALIDATE_NOT_APPLICABLE);

        verify(dcuMinSignOffProcess)
                .hasFinished(DCU_MIN_MINISTER_SIGN_OFF_END);

        verify(dcuMinSignOffProcess, never()).hasFinished(VALIDATE_REJECTION_NOTE);
    }
}
