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
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/TO_HOME_SEC.bpmn",
        "processes/TO_ENQUIRY_SUBJECT_REASON.bpmn",
        "processes/TO_CHANGE_BUSINESS_AREA.bpmn"
})
public class TO_HOME_SEC {

    // COMMON GATEWAY &  GATEWAY OUTCOMES
    private static final String DIRECTION = "DIRECTION";
    private static final String HOME_SEC_STATUS = "HomeSecStatus";
    private static final String BACKWARD = "BACKWARD";
    private static final String FORWARD = "FORWARD";
    private static final String SET_ENQUIRY = "SetEnquiry";
    private static final String CHANGE_BUSINESS_AREA = "ChangeBusinessArea";
    private static final String SAVE = "Save";
    private static final String SEND_TO_DRAFT = "SendToDraft";
    private static final String SEND_TO_TRIAGE = "SendToTriage";
    private static final String SEND_TO_DISPATCH = "SendToDispatch";
    private static final String SEND_TO_STOP_LIST = "SendToStopList";
    private static final String PUT_ON_CAMPAIGN = "PutOnCampaign";

    // USER AND SERVICE TASKS
    private static final String HOME_SEC_INPUT = "HOME_SEC_INPUT";
    private static final String TO_GET_CAMPAIGN_TYPE = "TO_GET_CAMPAIGN_TYPE";
    private static final String TO_GET_STOP_LIST = "TO_GET_STOP_LIST";
    private static final String TO_CASE_REJECTED = "CASE_REJECTED";
    private static final String SAVE_REJECT_CASE_NOTE = "Activity_0d5e4m4";

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
    public void shouldApproveAndComplete() {

        when(TOProcess.waitsAtUserTask(HOME_SEC_INPUT))
                .thenReturn(task -> task.complete(withVariables(HOME_SEC_STATUS, SEND_TO_DISPATCH)));

        Scenario.run(TOProcess)
                .startByKey("TO_HOME_SEC")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(HOME_SEC_INPUT);

        verify(TOProcess, times(0))
                .hasCompleted(TO_CASE_REJECTED);

        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJECT_CASE_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_CAMPAIGN_TYPE);

        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_STOP_LIST);

    }

    @Test
    public void shouldSaveThenApproveAndComplete() {

        when(TOProcess.waitsAtUserTask(HOME_SEC_INPUT))
                .thenReturn(task -> task.complete(withVariables(HOME_SEC_STATUS, SAVE)))
                .thenReturn(task -> task.complete(withVariables(HOME_SEC_STATUS, SEND_TO_DISPATCH)));

        Scenario.run(TOProcess)
                .startByKey("TO_HOME_SEC")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(HOME_SEC_INPUT);

        verify(TOProcess, times(0))
                .hasCompleted(TO_CASE_REJECTED);

        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJECT_CASE_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_CAMPAIGN_TYPE);

        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_STOP_LIST);

    }


    @Test
    public void shouldGetCampaignTypeAndGoBackThenForwardAndComplete() {

        when(TOProcess.waitsAtUserTask(HOME_SEC_INPUT))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, BACKWARD, HOME_SEC_STATUS, PUT_ON_CAMPAIGN)))
                .thenReturn(task -> task.complete(withVariables(HOME_SEC_STATUS, SEND_TO_DISPATCH)));

        when(TOProcess.waitsAtUserTask(TO_GET_CAMPAIGN_TYPE)).thenReturn(task -> task.complete());

        Scenario.run(TOProcess)
                .startByKey("TO_HOME_SEC")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_GET_CAMPAIGN_TYPE);

        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJECT_CASE_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(TO_CASE_REJECTED);

        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJECT_CASE_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_STOP_LIST);
    }

    @Test
    public void shouldGetStopListAndGoBackAndGoForwardAndComplete() {

        when(TOProcess.waitsAtUserTask(HOME_SEC_INPUT))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, BACKWARD, HOME_SEC_STATUS, SEND_TO_STOP_LIST)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, HOME_SEC_STATUS, SEND_TO_DISPATCH)));

        when(TOProcess.waitsAtUserTask(TO_GET_STOP_LIST)).thenReturn(task -> task.complete());

        Scenario.run(TOProcess)
                .startByKey("TO_HOME_SEC")
                .execute();

        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_CAMPAIGN_TYPE);

        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJECT_CASE_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(TO_CASE_REJECTED);

        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJECT_CASE_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_CAMPAIGN_TYPE);

        verify(TOProcess, times(1))
                .hasCompleted(TO_GET_STOP_LIST);
    }

    @Test
    public void testRejectCaseBackThenRejectCaseForward() {
        when(TOProcess.waitsAtUserTask(HOME_SEC_INPUT))
                .thenReturn(task -> task.complete(withVariables(HOME_SEC_STATUS, SEND_TO_DRAFT, DIRECTION, FORWARD)))
                .thenReturn(task -> task.complete(withVariables(HOME_SEC_STATUS, SEND_TO_TRIAGE, DIRECTION, FORWARD)));

        when(TOProcess.waitsAtUserTask(TO_CASE_REJECTED))
                .thenReturn(task -> task.complete(withVariables(DIRECTION,BACKWARD)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION,FORWARD)));

        Scenario.run(TOProcess)
                .startByKey("TO_HOME_SEC")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(HOME_SEC_INPUT);

        verify(TOProcess, times(2))
                .hasCompleted(TO_CASE_REJECTED);

        verify(TOProcess, times(1))
                .hasCompleted(SAVE_REJECT_CASE_NOTE);

    }
}
