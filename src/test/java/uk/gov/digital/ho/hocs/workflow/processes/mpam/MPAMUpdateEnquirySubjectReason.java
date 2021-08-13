package uk.gov.digital.ho.hocs.workflow.processes.mpam;

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
@Deployment(resources = "processes/MPAM/MPAM_UPDATE_ENQUIRY_SUBJECT_REASON.bpmn")
public class MPAMUpdateEnquirySubjectReason {

    private static final String MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN = "MPAM_UPDATE_ENQUIRY_SUBJECT_REASON";

    private static final String MPAM_ENQUIRY_SUBJECT_ACTIVITY = "MpamEnquirySubject_Activity";
    private static final String MPAM_ENQUIRY_REASON_ACTIVITY = "MpamEnquiryReason_Activity";
    private static final String EU_NATIONAL_DETAILS_ENQUIRY_REASON_ACTIVITY = "EuNationalDetailsEnquiryReason_Activity";
    private static final String CLEAR_TEMP_ENQUIRY_SUBJECT_ACTIVITY = "ClearTempEnquirySubject_Activity";
    private static final String CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY = "ClearTempEnquirySubjectReason_Activity";
    private static final String UPDATE_ENQUIRY_SUBJECT_REASON_ACTIVITY = "UpdateEnquirySubjectReason_Activity";
    private static final String CLEAR_EU_NATIONAL_COMPLIANCE_REASONS_ACTIVITY = "ClearEuNationalComplianceReasons_Activity";
    private static final String CLEAR_EU_NATIONAL_OTHER_REASON_ACTIVITY = "ClearEuNationalOtherReason_Activity";
    private static final String CREATE_EU_NATIONAL_OTHER_REASON_CASE_NOTE = "CreateEuNationalOtherReasonCaseNote_Activity";

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
    public void whenEnquiryReasonEuNational_detailsScreen() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true")));

        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true",
                            "EnquiryReason", "EuNationalComplianceMeasures")));

        when(processScenario.waitsAtUserTask(EU_NATIONAL_DETAILS_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasCompleted(MPAM_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(UPDATE_ENQUIRY_SUBJECT_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(EU_NATIONAL_DETAILS_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_EU_NATIONAL_OTHER_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY);
    }

    @Test
    public void whenEnquiryReasonEuNational_detailsScreen_withOther() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true")));

        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true",
                        "EnquiryReason", "EuNationalComplianceMeasures")));

        when(processScenario.waitsAtUserTask(EU_NATIONAL_DETAILS_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "ComplianceMeasuresOtherDetails", "TEST")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario).hasCompleted(MPAM_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(UPDATE_ENQUIRY_SUBJECT_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(EU_NATIONAL_DETAILS_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(CREATE_EU_NATIONAL_OTHER_REASON_CASE_NOTE);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY);
    }

    @Test
    public void whenEnquiryReasonEuNational_detailsScreen_backThenProgress() {
        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_SUBJECT_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true")));

        when(processScenario.waitsAtUserTask(MPAM_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", "true",
                        "EnquiryReason", "EuNationalComplianceMeasures")));

        when(processScenario.waitsAtUserTask(EU_NATIONAL_DETAILS_ENQUIRY_REASON_ACTIVITY))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD")));

        Scenario.run(processScenario)
                .startByKey(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_BPMN)
                .execute();

        verify(processScenario).hasCompleted(MPAM_ENQUIRY_SUBJECT_ACTIVITY);
        verify(processScenario, times(2)).hasCompleted(MPAM_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario, times(2)).hasCompleted(UPDATE_ENQUIRY_SUBJECT_REASON_ACTIVITY);
        verify(processScenario, times(2)).hasCompleted(EU_NATIONAL_DETAILS_ENQUIRY_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_EU_NATIONAL_OTHER_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY);
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
        verify(processScenario).hasCompleted(CLEAR_EU_NATIONAL_COMPLIANCE_REASONS_ACTIVITY);
        verify(processScenario).hasCompleted(CLEAR_TEMP_ENQUIRY_SUBJECT_REASON_ACTIVITY);
        verify(processScenario).hasCompleted(MPAM_UPDATE_ENQUIRY_SUBJECT_REASON_END_EVENT);
    }

}
