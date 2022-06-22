package uk.gov.digital.ho.hocs.workflow.processes.foi;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/STAGE.bpmn",
        "processes/FOI/FOI.bpmn",
        "processes/FOI/FOI_CASE_CREATION.bpmn",
        "processes/FOI/FOI_ALLOCATION.bpmn",
        "processes/FOI/FOI_ACCEPTANCE.bpmn",
        "processes/FOI/FOI_APPROVAL.bpmn",
        "processes/FOI/FOI_DRAFT.bpmn",
        "processes/FOI/FOI_DISPATCH.bpmn",
        "processes/FOI/FOI_SOFT_CLOSE.bpmn"
})
public class FOI {

    // Stages
    public static final String CASE_CREATION_ACTIVITY = "FOI_CASE_CREATION";
    public static final String ALLOCATION_ACTIVITY = "FOI_ALLOCATION";
    public static final String ACCEPTANCE_ACTIVITY = "FOI_ACCEPTANCE";
    public static final String DRAFT_ACTIVITY = "FOI_DRAFT";
    public static final String APPROVAL_ACTIVITY = "FOI_APPROVAL";
    public static final String DISPATCH_ACTIVITY = "FOI_DISPATCH";
    public static final String SOFT_CLOSE_ACTIVITY = "FOI_SOFT_CLOSE";

    // Service Tasks
    public static final String STICKY_CASES_FOR_ALLOCATION = "StickyCasesForAllocation";
    public static final String STICKY_CASES_FOR_APPROVAL = "StickyCasesForApproval";
    public static final String STICKY_CASES_FOR_DISPATCH = "StickyCasesForDispatch";
    public static final String STICKY_CASES_FOR_CLOSE = "StickyCasesForClose";
    public static final String STICKY_CASES_FOR_NON_VALID_CLOSE = "StickyCasesForNonValidClose";
    public static final String COMPLETE_CASE = "CaseComplete";

    // Gates
    public static final String IS_CASE_VALID_GATE = "is_case_valid";
    public static final String IS_CASE_ACCEPTED_GATE = "is_case_accepted";
    public static final String SHOULD_DISPATCH_GATE = "should_dispatch";


    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario FOIProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void happyPath() {

        whenAtCallActivity(CASE_CREATION_ACTIVITY)
                .alwaysReturn("RequestValidity", "RequestValid-Y")
                .deploy(rule);

        whenAtCallActivity(ALLOCATION_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(ACCEPTANCE_ACTIVITY)
                .alwaysReturn("AcceptCase", "Y")
                .deploy(rule);

        whenAtCallActivity(DRAFT_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(APPROVAL_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(DISPATCH_ACTIVITY)
                .alwaysReturn("ShouldDispatch", "ShouldDispatch-Y")
                .deploy(rule);

        whenAtCallActivity(SOFT_CLOSE_ACTIVITY)
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        whenAtCallActivity(COMPLETE_CASE)
                .deploy(rule);

        whenAtCallActivity(COMPLETE_CASE)
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(IS_CASE_VALID_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_ALLOCATION);

        verify(FOIProcess, times(1))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(IS_CASE_ACCEPTED_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(DRAFT_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_APPROVAL);

        verify(FOIProcess, times(1))
                .hasCompleted(APPROVAL_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_DISPATCH);

        verify(FOIProcess, times(1))
                .hasCompleted(DISPATCH_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(SHOULD_DISPATCH_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_CLOSE);

        verify(FOIProcess, times(0))
                .hasCompleted(STICKY_CASES_FOR_NON_VALID_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(SOFT_CLOSE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(bpmnService).completeCase(any());

    }

    @Test
    public void invalidCase() {

        whenAtCallActivity(CASE_CREATION_ACTIVITY)
                .thenReturn("RequestValidity", "RequestValid-N")
                .deploy(rule);

        whenAtCallActivity(SOFT_CLOSE_ACTIVITY)
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        whenAtCallActivity(COMPLETE_CASE)
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(IS_CASE_VALID_GATE);

        verify(FOIProcess, times(0))
                .hasCompleted(STICKY_CASES_FOR_ALLOCATION);

        verify(FOIProcess, times(0))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(0))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(0))
                .hasCompleted(IS_CASE_ACCEPTED_GATE);

        verify(FOIProcess, times(0))
                .hasCompleted(DRAFT_ACTIVITY);

        verify(FOIProcess, times(0))
                .hasCompleted(STICKY_CASES_FOR_APPROVAL);

        verify(FOIProcess, times(0))
                .hasCompleted(APPROVAL_ACTIVITY);

        verify(FOIProcess, times(0))
                .hasCompleted(STICKY_CASES_FOR_DISPATCH);

        verify(FOIProcess, times(0))
                .hasCompleted(DISPATCH_ACTIVITY);

        verify(FOIProcess, times(0))
                .hasCompleted(SHOULD_DISPATCH_GATE);

        verify(FOIProcess, times(0))
                .hasCompleted(STICKY_CASES_FOR_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_NON_VALID_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(SOFT_CLOSE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(bpmnService).completeCase(any());
    }

    @Test
    public void caseRejectedAtAcceptanceThenComplete() {

        whenAtCallActivity(CASE_CREATION_ACTIVITY)
                .thenReturn("RequestValidity", "RequestValid-Y")
                .deploy(rule);

        whenAtCallActivity(ALLOCATION_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(ACCEPTANCE_ACTIVITY)
                .thenReturn("AcceptCase", "N")
                .thenReturn("AcceptCase", "Y")
                .deploy(rule);

        whenAtCallActivity(DRAFT_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(APPROVAL_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(DISPATCH_ACTIVITY)
                .alwaysReturn("ShouldDispatch", "ShouldDispatch-Y")
                .deploy(rule);

        whenAtCallActivity(SOFT_CLOSE_ACTIVITY)
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        whenAtCallActivity(COMPLETE_CASE)
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(IS_CASE_VALID_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_ALLOCATION);

        verify(FOIProcess, times(2))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(IS_CASE_ACCEPTED_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(DRAFT_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_APPROVAL);

        verify(FOIProcess, times(1))
                .hasCompleted(APPROVAL_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_DISPATCH);

        verify(FOIProcess, times(1))
                .hasCompleted(DISPATCH_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(SHOULD_DISPATCH_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_CLOSE);

        verify(FOIProcess, times(0))
                .hasCompleted(STICKY_CASES_FOR_NON_VALID_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(SOFT_CLOSE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(bpmnService).completeCase(any());
    }

    @Test
    public void caseInvalidThenReopenedAndCompletedAsValid() {
        whenAtCallActivity(CASE_CREATION_ACTIVITY)
                .thenReturn("RequestValidity", "RequestValid-N")
                .thenReturn("RequestValidity", "RequestValid-Y")
                .deploy(rule);

        whenAtCallActivity(ALLOCATION_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(ACCEPTANCE_ACTIVITY)
                .alwaysReturn("AcceptCase", "Y")
                .deploy(rule);

        whenAtCallActivity(DRAFT_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(APPROVAL_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(DISPATCH_ACTIVITY)
                .alwaysReturn("ShouldDispatch", "ShouldDispatch-Y")
                .deploy(rule);

        whenAtCallActivity(SOFT_CLOSE_ACTIVITY)
                .thenReturn("","")
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        whenAtCallActivity(COMPLETE_CASE)
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(2))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(IS_CASE_VALID_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_ALLOCATION);

        verify(FOIProcess, times(1))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(IS_CASE_ACCEPTED_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(DRAFT_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_APPROVAL);

        verify(FOIProcess, times(1))
                .hasCompleted(APPROVAL_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_DISPATCH);

        verify(FOIProcess, times(1))
                .hasCompleted(DISPATCH_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(SHOULD_DISPATCH_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_NON_VALID_CLOSE);

        verify(FOIProcess, times(2))
                .hasCompleted(SOFT_CLOSE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(bpmnService).completeCase(any());
    }

    @Test
    public void caseRejectedAtDispatchReturnToApproval() {

        whenAtCallActivity(CASE_CREATION_ACTIVITY)
                .alwaysReturn("RequestValidity", "RequestValid-Y")
                .deploy(rule);

        whenAtCallActivity(ALLOCATION_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(ACCEPTANCE_ACTIVITY)
                .alwaysReturn("AcceptCase", "Y")
                .deploy(rule);

        whenAtCallActivity(DRAFT_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(APPROVAL_ACTIVITY)
                .deploy(rule);

        whenAtCallActivity(DISPATCH_ACTIVITY)
                .thenReturn("ShouldDispatch", "ShouldDispatch-N")
                .thenReturn("ShouldDispatch", "ShouldDispatch-Y")
                .deploy(rule);

        whenAtCallActivity(SOFT_CLOSE_ACTIVITY)
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        whenAtCallActivity(COMPLETE_CASE)
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(IS_CASE_VALID_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_ALLOCATION);

        verify(FOIProcess, times(1))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(IS_CASE_ACCEPTED_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(DRAFT_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_APPROVAL);

        verify(FOIProcess, times(2))
                .hasCompleted(APPROVAL_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(STICKY_CASES_FOR_DISPATCH);

        verify(FOIProcess, times(2))
                .hasCompleted(DISPATCH_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(SHOULD_DISPATCH_GATE);

        verify(FOIProcess, times(1))
                .hasCompleted(STICKY_CASES_FOR_CLOSE);

        verify(FOIProcess, times(0))
                .hasCompleted(STICKY_CASES_FOR_NON_VALID_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(SOFT_CLOSE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(bpmnService).completeCase(any());
    }
}
