package uk.gov.digital.ho.hocs.workflow.processes.mpam;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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
import uk.gov.digital.ho.hocs.workflow.processes.MPAMCommonTests;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM/MPAM_UPDATE_ENQUIRY_SUBJECT_REASON.bpmn")
public class MPAMUpdateEnquirySubjectReason {

    private static final String MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN = "MPAM_UPDATE_ENQUIRY_SUBJECT_REASON";

    private static final String MPAM_ENQUIRY_SUBJECT_ACTIVITY = "MpamEnquirySubject_Activity";
    private static final String MPAM_ENQUIRY_REASON_ACTIVITY = "MpamEnquiryReason_Activity";
    private static final String CLEAR_TEMP_ENQUIRY_SUBJECT_ACTIVITY = "ClearTempEnquirySubject_Activity";
    private static final String CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY = "ClearTempEnquirySubjectReason_Activity";
    private static final String UPDATE_ENQUIRY_SUBJECT_REASON_ACTIVITY = "UpdateEnquirySubjectReason_Activity";

    private static final String MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_END_EVENT = "MpamUpdateEnquirySubjectReason_EndEvent";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(1d)
            .build();

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
    public void whenEnquirySubjectBackPressed_thenClearCalledAndEndEvent() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasFinished(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_END_EVENT);
    }

    @Test
    public void whenEnquirySubjectInvalidDirection_thenClearCalledAndEndEvent() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "TEST")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasFinished(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_END_EVENT);
    }

    @Test
    public void whenEnquirySubjectSubmittedInvalid_thenReloadsActivity() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "false")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario, times(2)).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
    }

    @Test
    public void whenEnquiryReasonBackPressed_thenClearCalledAndEndEvent() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true")));

        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasCompleted(MPAM_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY);
        verify(processScenario).hasFinished(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_END_EVENT);
    }

    @Test
    public void whenEnquiryReasonInvalidDirection_thenClearCalledAndEndEvent() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true")));

        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "TEST")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasCompleted(MPAM_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY);
        verify(processScenario).hasFinished(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_END_EVENT);
    }

    @Test
    public void whenEnquiryReasonInvalidSubmitted_thenReloadActivity() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true")));

        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "false")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARD")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario, times(2)).hasCompleted(MPAM_ENQUIRY_REASON_ACTIVITY);
    }


    @Test
    public void happyPath() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true")));

        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasCompleted(MPAM_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(UPDATE_ENQUIRY_SUBJECT_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_END_EVENT);
    }

}
