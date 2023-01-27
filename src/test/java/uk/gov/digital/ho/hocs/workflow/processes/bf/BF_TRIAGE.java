package uk.gov.digital.ho.hocs.workflow.processes.bf;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.BACKWARD;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.DIRECTION;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.FORWARD;
import static uk.gov.digital.ho.hocs.workflow.api.WorkflowConstants.VALID;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/BF/BF_TRIAGE.bpmn")
public class BF_TRIAGE {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario process;

    private static final String PAYMENT_TYPE_CONSOLATORY = "PaymentTypeConsolatory";

    private static final String PAYMENT_TYPE_EXGRATIA = "PaymentTypeExGratia";

    private static final String PAYMENT_REQUESTED = "PaymentRequested";

    private static final String YES = "Yes";

    private static final String NO = "No";

    private static final String EMPTY_STRING = "";

    // PROCESSES
    private static final String CLEAR_EXGRATIA_VAL = "CLEAR_EXGRATIA_VAL";

    private static final String CLEAR_CONSOL_VAL = "CLEAR_CONSOL_VAL";

    private static final String CLEAR_PAYMENT_VALS = "CLEAR_PAYMENT_VALS";

    private static final String CALCULATE_TOTAL_PAYMENT = "CALCULATE_TOTAL_PAYMENT";

    private static final String CLEAR_REQUESTED_AMOUNT = "CLEAR_REQUESTED_AMOUNT";

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testHappyPath() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, false, "BfTriageAccept", "Yes"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason")).thenReturn(task -> task.complete(
            withVariables(VALID, false, "BFTriageResult", "Pending", PAYMENT_TYPE_CONSOLATORY, EMPTY_STRING,
                PAYMENT_TYPE_EXGRATIA, EMPTY_STRING, PAYMENT_REQUESTED, EMPTY_STRING))).thenReturn(
            task -> task.complete(
                withVariables(VALID, true, "BFTriageResult", "Pending", PAYMENT_TYPE_CONSOLATORY, EMPTY_STRING,
                    PAYMENT_TYPE_EXGRATIA, EMPTY_STRING, PAYMENT_REQUESTED, EMPTY_STRING))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BFTriageResult", "Pending", PAYMENT_TYPE_CONSOLATORY, NO,
                PAYMENT_TYPE_EXGRATIA, NO, PAYMENT_REQUESTED, NO))).thenReturn(task -> task.complete(
            withVariables(VALID, true, "BFTriageResult", "Draft", PAYMENT_TYPE_CONSOLATORY, NO, PAYMENT_TYPE_EXGRATIA,
                NO, PAYMENT_REQUESTED, NO)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(3)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(0)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(0)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(0)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(3)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testTriageOfflineCaseTransfer() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, false, "BfTriageAccept", "No"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "No")));

        when(process.waitsAtUserTask("Transfer_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, false, DIRECTION, FORWARD))).thenReturn(
            task -> task.complete(withVariables(DIRECTION, BACKWARD, VALID, false))).thenReturn(task -> task.complete(
            withVariables(DIRECTION, FORWARD, VALID, true, "CaseNote_TriageTransfer", "Reject note")));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(0)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(0)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(0)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(0)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(0)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process, times(1)).hasCompleted("EndEvent_BF_TRIAGE");
        verify(process, times(1)).hasCompleted("Save_Offline_Case_Transfer_Note");
        verify(bpmnService, times(1)).updateAllocationNote(any(), any(), eq("Reject note"),
            eq("OFFLINE_CASE_TRANSFER"));
    }

    @Test
    public void testReasonsBackThenComplete() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason")).thenReturn(
            task -> task.complete(withVariables(VALID, false, DIRECTION, BACKWARD))).thenReturn(task -> task.complete(
            withVariables(VALID, true, "BFTriageResult", "Draft", PAYMENT_TYPE_CONSOLATORY, EMPTY_STRING,
                PAYMENT_TYPE_EXGRATIA, EMPTY_STRING, PAYMENT_REQUESTED, EMPTY_STRING)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(1)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(0)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(0)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(0)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(1)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testCompleteComplaint() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason")).thenReturn(task -> task.complete(
            withVariables(VALID, true, "BFTriageResult", "Complete", PAYMENT_TYPE_CONSOLATORY, EMPTY_STRING,
                PAYMENT_TYPE_EXGRATIA, EMPTY_STRING, PAYMENT_REQUESTED, EMPTY_STRING))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BFTriageResult", "Complete", PAYMENT_TYPE_CONSOLATORY, NO,
                PAYMENT_TYPE_EXGRATIA, NO, PAYMENT_REQUESTED, NO)));

        when(process.waitsAtUserTask("Validate_Complete_Reason")).thenReturn(
            task -> task.complete(withVariables(VALID, true, DIRECTION, BACKWARD))).thenReturn(
            task -> task.complete(withVariables(VALID, false, DIRECTION, FORWARD))).thenReturn(
            task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(2)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(0)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(0)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(0)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(2)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testEscalate() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason")).thenReturn(task -> task.complete(
            withVariables(VALID, true, DIRECTION, FORWARD, "BFTriageResult", "Escalate", PAYMENT_TYPE_CONSOLATORY, NO,
                PAYMENT_TYPE_EXGRATIA, NO, PAYMENT_REQUESTED, NO

                         )));

        when(process.waitsAtUserTask("Validate_Escalate")).thenReturn(
            task -> task.complete(withVariables(VALID, false, "BFTriageResult", "Pending"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, DIRECTION, BACKWARD))).thenReturn(
            task -> task.complete(withVariables(VALID, true, DIRECTION, FORWARD)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(2)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(0)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(0)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(0)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(2)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process).hasCompleted("Save_Note");
        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testPaymentCalcConsolOnlyPath() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, false, "BfTriageAccept", "Yes"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason")).thenReturn(task -> task.complete(
            withVariables(VALID, true, "BFTriageResult", "Draft", PAYMENT_TYPE_CONSOLATORY, YES, PAYMENT_REQUESTED,
                NO)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(0)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(0)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(1)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(1)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(1)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testPaymentCalcExGratiaOnlyPath() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, false, "BfTriageAccept", "Yes"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason")).thenReturn(task -> task.complete(
            withVariables(VALID, true, "BFTriageResult", "Draft", PAYMENT_TYPE_EXGRATIA, YES, PAYMENT_REQUESTED, NO)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(0)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(1)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(0)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(1)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(1)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testPaymentCalcConsolAndExGratiaPath() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, false, "BfTriageAccept", "Yes"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes"))).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason")).thenReturn(task -> task.complete(
            withVariables(VALID, true, "BFTriageResult", "Draft", PAYMENT_TYPE_CONSOLATORY, YES, PAYMENT_TYPE_EXGRATIA,
                YES, PAYMENT_REQUESTED, NO)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();
        verify(process, times(0)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(0)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(0)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(1)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(1)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testPaymentRequestedYes() {

        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "Yes")));

        when(process.waitsAtUserTask("Validate_Capture_Reason")).thenReturn(task -> task.complete(
            withVariables(VALID, true, "BFTriageResult", "Draft", PAYMENT_TYPE_CONSOLATORY, NO, PAYMENT_TYPE_EXGRATIA,
                NO, PAYMENT_REQUESTED, YES)));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();

        verify(process, times(1)).hasCompleted(CLEAR_PAYMENT_VALS);
        verify(process, times(0)).hasCompleted(CLEAR_CONSOL_VAL);
        verify(process, times(0)).hasCompleted(CLEAR_EXGRATIA_VAL);
        verify(process, times(0)).hasCompleted(CALCULATE_TOTAL_PAYMENT);
        verify(process, times(0)).hasCompleted(CLEAR_REQUESTED_AMOUNT);

        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

    @Test
    public void testEscalateToPsu() {
        when(process.waitsAtUserTask("Validate_Accept_Case")).thenReturn(
            task -> task.complete(withVariables(VALID, true, "BfTriageAccept", "PSU")));

        Scenario.run(process).startByKey("BF_TRIAGE").execute();

        verify(process).hasCompleted("Validate_Accept_Case");
        verify(process).hasCompleted("EndEvent_BF_TRIAGE");
    }

}
