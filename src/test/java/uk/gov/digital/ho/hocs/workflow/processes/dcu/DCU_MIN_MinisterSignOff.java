package uk.gov.digital.ho.hocs.workflow.processes.dcu;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/DCU/DCU_MIN_MINISTER_SIGN_OFF.bpmn")
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
        final String REJECTION_REASON = "Rejection Reason";

        when(dcuMinSignOffProcess.waitsAtUserTask(APPROVE_MINISTER_SIGN_OFF))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "REJECT")));

        when(dcuMinSignOffProcess.waitsAtUserTask(VALIDATE_REJECTION_NOTE))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_MinisterReject", REJECTION_REASON)));


        Scenario.run(dcuMinSignOffProcess)
                .startByKey("DCU_MIN_MINISTER_SIGN_OFF")
                .execute();

        verify(dcuMinSignOffProcess)
                .hasFinished(VALIDATE_REJECTION_NOTE);

        verify(dcuMinSignOffProcess)
                .hasFinished(DCU_MIN_MINISTER_SIGN_OFF_END);

        verify(dcuMinSignOffProcess, never()).hasFinished(VALIDATE_NOT_APPLICABLE);

        verify(bpmnService).updateAllocationNote(any(), any(), eq(REJECTION_REASON), eq("REJECT"));

    }

    @Test
    public void notApplicableScenario() {
        final String NA_REASON = "N/A Reason";

        when(dcuMinSignOffProcess.waitsAtUserTask(APPROVE_MINISTER_SIGN_OFF))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "NOT_APPLICABLE")));

        when(dcuMinSignOffProcess.waitsAtUserTask(VALIDATE_NOT_APPLICABLE))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD", "CaseNote_MinisterNotApplicable", NA_REASON)));

        Scenario.run(dcuMinSignOffProcess)
                .startByKey("DCU_MIN_MINISTER_SIGN_OFF")
                .execute();

        verify(dcuMinSignOffProcess)
                .hasFinished(VALIDATE_NOT_APPLICABLE);

        verify(dcuMinSignOffProcess)
                .hasFinished(DCU_MIN_MINISTER_SIGN_OFF_END);

        verify(dcuMinSignOffProcess, never()).hasFinished(VALIDATE_REJECTION_NOTE);

        verify(bpmnService).updateAllocationNote(any(), any(), eq(NA_REASON), eq("REJECT"));
    }

    @Test
    public void notApplicableScenarioBackLinkGoesBackToMinisterSignOff() {

        when(dcuMinSignOffProcess.waitsAtUserTask(APPROVE_MINISTER_SIGN_OFF))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "NOT_APPLICABLE")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "MinisterSignOffDecision", "ACCEPT")));

        when(dcuMinSignOffProcess.waitsAtUserTask(VALIDATE_NOT_APPLICABLE))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD")));

        Scenario.run(dcuMinSignOffProcess)
                .startByKey("DCU_MIN_MINISTER_SIGN_OFF")
                .execute();

        verify(dcuMinSignOffProcess, times(2))
                .hasFinished(APPROVE_MINISTER_SIGN_OFF);

        verify(dcuMinSignOffProcess, times(1))
                .hasFinished(VALIDATE_NOT_APPLICABLE);

        verify(dcuMinSignOffProcess)
                .hasFinished(DCU_MIN_MINISTER_SIGN_OFF_END);

        verify(bpmnService, never()).updateAllocationNote(any(), any(), any(), any());
    }
}
