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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/STAGE.bpmn",
        "processes/TO.bpmn",
        "processes/TO_TRIAGE.bpmn",
        "processes/TO_DRAFT.bpmn",
        "processes/TO_QA.bpmn",
        "processes/TO_CAMPAIGN.bpmn",
        "processes/TO_STOP_LIST.bpmn",
        "processes/TO_HOME_SEC.bpmn",
        "processes/TO_DISPATCH.bpmn"
})
public class TO {

    // STAGES
    private static final String DATA_INPUT = "TO_DATA_INPUT";
    private static final String TRIAGE = "TO_TRIAGE";
    private static final String QA = "TO_QA";
    private static final String CAMPAIGN = "TO_CAMPAIGN";
    private static final String STOP_LIST = "TO_STOP_LIST";
    private static final String DRAFT = "TO_DRAFT";
    private static final String HOME_SEC = "TO_HOME_SEC";
    private static final String DISPATCH = "TO_DISPATCH";

    // SERVICE TASKS
    private static final String START = "TO_START";
    private static final String COMPLETE_CASE = "COMPLETE_CASE";
    private static final String END = "TO_END";

    // COMMON GATEWAY OUTCOMES
    private static final String PUT_ON_CAMPAIGN = "PutOnCampaign";
    private static final String SEND_TO_TRIAGE = "SendToTriage";
    private static final String SEND_TO_DRAFT = "SendToDraft";
    private static final String SEND_TO_QA = "SendToQA";
    private static final String SEND_TO_DISPATCH = "SendToDispatch";
    private static final String SEND_TO_STOP_LIST = "SendToStopList";
    private static final String REJECT_DRAFT = "RejectDraft";

    private static final Map<String, Object> PROCESS_INSTANCE_VARS = new HashMap<>();

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
        PROCESS_INSTANCE_VARS.put("StageUUID", "RANDOM_UUID_AS_STRING");
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testHappyPath() {

        whenAtCallActivity(DATA_INPUT)
                .deploy(rule);

        whenAtCallActivity(TRIAGE)
                .alwaysReturn("BusAreaStatus", "Confirmed")
                .alwaysReturn("TriageOutcome", SEND_TO_DRAFT)
                .deploy(rule);

        whenAtCallActivity(DRAFT)
                .alwaysReturn("BusAreaStatus", "Confirmed")
                .alwaysReturn("DraftStatus", SEND_TO_QA)
                .deploy(rule);

        whenAtCallActivity(QA)
                .alwaysReturn("QaStatus", SEND_TO_DISPATCH, "HomeSecInt", "")
                .deploy(rule);

        whenAtCallActivity(DISPATCH)
                .alwaysReturn("DispatchStatus", "Dispatched")
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO", PROCESS_INSTANCE_VARS)
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(START);

        verify(TOProcess, times(1))
                .hasCompleted(DATA_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TRIAGE);

        verify(TOProcess, times(1))
                .hasCompleted(DRAFT);

        verify(TOProcess, times(1))
                .hasCompleted(QA);

        verify(TOProcess, times(1))
                .hasCompleted(DISPATCH);

        verify(TOProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(TOProcess, times(1))
                .hasCompleted(END);

        // NOT CALLED CHECK
        verify(TOProcess, times(0))
                .hasCompleted(CAMPAIGN);

        verify(TOProcess, times(0))
                .hasCompleted(STOP_LIST);

        // CASE COMPLETED
        verify(bpmnService).completeCase(any());
    }

    @Test
    public void testHomeSec() {

        whenAtCallActivity(DATA_INPUT)
                .deploy(rule);

        whenAtCallActivity(TRIAGE)
                .alwaysReturn("BusAreaStatus", "Confirmed")
                .alwaysReturn("TriageOutcome", SEND_TO_DRAFT)
                .deploy(rule);

        whenAtCallActivity(DRAFT)
                .alwaysReturn("BusAreaStatus", "Confirmed")
                .alwaysReturn("DraftStatus", SEND_TO_QA)
                .deploy(rule);

        whenAtCallActivity(QA)
                .alwaysReturn("QaStatus", SEND_TO_DISPATCH)
                .alwaysReturn("HomeSecInt", "Yes")
                .deploy(rule);

        whenAtCallActivity(HOME_SEC)
                .alwaysReturn("HomeSecStatus", SEND_TO_DISPATCH)
                .deploy(rule);

        whenAtCallActivity(DISPATCH)
                .alwaysReturn("DispatchStatus", "Dispatched")
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO", PROCESS_INSTANCE_VARS)
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(START);

        verify(TOProcess, times(1))
                .hasCompleted(DATA_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TRIAGE);

        verify(TOProcess, times(1))
                .hasCompleted(DRAFT);

        verify(TOProcess, times(1))
                .hasCompleted(QA);

        verify(TOProcess, times(1))
                .hasCompleted(HOME_SEC);

        verify(TOProcess, times(1))
                .hasCompleted(DISPATCH);

        verify(TOProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(TOProcess, times(1))
                .hasCompleted(END);

        // NOT CALLED CHECK
        verify(TOProcess, times(0))
                .hasCompleted(CAMPAIGN);

        verify(TOProcess, times(0))
                .hasCompleted(STOP_LIST);

        // CASE COMPLETED
        verify(bpmnService).completeCase(any());

    }

    @Test
    public void testBypassQA() {

        whenAtCallActivity(DATA_INPUT)
                .deploy(rule);

        whenAtCallActivity(TRIAGE)
                .alwaysReturn("BusAreaStatus", "Confirmed")
                .alwaysReturn("TriageOutcome", SEND_TO_DRAFT)
                .deploy(rule);

        whenAtCallActivity(DRAFT)
                .alwaysReturn("BusAreaStatus", "Confirmed")
                .alwaysReturn("DraftStatus", SEND_TO_DISPATCH)
                .deploy(rule);

        whenAtCallActivity(DISPATCH)
                .alwaysReturn("DispatchStatus", "Dispatched")
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO", PROCESS_INSTANCE_VARS)
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(START);

        verify(TOProcess, times(1))
                .hasCompleted(DATA_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TRIAGE);

        verify(TOProcess, times(1))
                .hasCompleted(DRAFT);

        verify(TOProcess, times(1))
                .hasCompleted(DISPATCH);

        verify(TOProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(TOProcess, times(1))
                .hasCompleted(END);

        // NOT CALLED CHECK
        verify(TOProcess, times(0))
                .hasCompleted(CAMPAIGN);

        verify(TOProcess, times(0))
                .hasCompleted(STOP_LIST);

        verify(TOProcess, times(0))
                .hasCompleted(QA);

        // CASE COMPLETED
        verify(bpmnService).completeCase(any());
    }

    @Test
    public void testAllPutOnCampaign() {

        whenAtCallActivity(DATA_INPUT)
                .deploy(rule);

        whenAtCallActivity(TRIAGE)
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "TriageOutcome", PUT_ON_CAMPAIGN
                )
                .deploy(rule);

        whenAtCallActivity(DRAFT)
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", PUT_ON_CAMPAIGN
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .deploy(rule);

        whenAtCallActivity(QA)
                .thenReturn(
                        "HomeSecInt", "Yes",
                        "QaStatus", PUT_ON_CAMPAIGN
                )
                .thenReturn(
                        "HomeSecInt", "Yes",
                        "QaStatus", SEND_TO_DISPATCH
                )
                .thenReturn(
                        "HomeSecInt", "Yes",
                        "QaStatus", SEND_TO_DISPATCH
                )
                .thenReturn(
                        "HomeSecInt", "No",
                        "QaStatus", SEND_TO_DISPATCH
                )
                .deploy(rule);

        whenAtCallActivity(HOME_SEC)
                .thenReturn("HomeSecStatus", PUT_ON_CAMPAIGN)
                .thenReturn("HomeSecStatus", SEND_TO_DISPATCH)
                .deploy(rule);

        whenAtCallActivity(DISPATCH)
                .thenReturn("DispatchStatus", PUT_ON_CAMPAIGN)
                .thenReturn("DispatchStatus", "Dispatched")
                .deploy(rule);

        whenAtCallActivity(CAMPAIGN)
                .thenReturn("BusAreaStatus", "Transferred")
                .thenReturn("CampaignOutcome", SEND_TO_DRAFT, "BusAreaStatus", "Confirmed")
                .thenReturn("CampaignOutcome", SEND_TO_DRAFT)
                .thenReturn("CampaignOutcome", SEND_TO_DRAFT)
                .thenReturn("CampaignOutcome", SEND_TO_DRAFT)
                .thenReturn("CampaignOutcome", SEND_TO_DRAFT)
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO", PROCESS_INSTANCE_VARS)
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(START);

        verify(TOProcess, times(1))
                .hasCompleted(DATA_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TRIAGE);

        verify(TOProcess, times(6))
                .hasCompleted(CAMPAIGN);

        verify(TOProcess, times(5))
                .hasCompleted(DRAFT);

        verify(TOProcess, times(4))
                .hasCompleted(QA);

        verify(TOProcess, times(2))
                .hasCompleted(HOME_SEC);

        verify(TOProcess, times(2))
                .hasCompleted(DISPATCH);

        verify(TOProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(TOProcess, times(1))
                .hasCompleted(END);

        // NOT CALLED CHECK
        verify(TOProcess, times(0))
                .hasCompleted(STOP_LIST);

        // CASE COMPLETED
        verify(bpmnService).completeCase(any());

    }

    @Test
    public void testAllSendToStopList() {

        whenAtCallActivity(DATA_INPUT)
                .deploy(rule);

        whenAtCallActivity(TRIAGE)
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "TriageOutcome", SEND_TO_STOP_LIST
                )
                .deploy(rule);

        whenAtCallActivity(DRAFT)
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_STOP_LIST
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .deploy(rule);

        whenAtCallActivity(QA)
                .thenReturn(
                        "HomeSecInt", "Yes",
                        "QaStatus", SEND_TO_STOP_LIST
                )
                .thenReturn(
                        "HomeSecInt", "Yes",
                        "QaStatus", SEND_TO_DISPATCH
                )
                .thenReturn(
                        "HomeSecInt", "Yes",
                        "QaStatus", SEND_TO_DISPATCH
                )
                .thenReturn(
                        "HomeSecInt", "No",
                        "QaStatus", SEND_TO_DISPATCH
                )
                .deploy(rule);

        whenAtCallActivity(HOME_SEC)
                .thenReturn("HomeSecStatus", SEND_TO_STOP_LIST)
                .thenReturn("HomeSecStatus", SEND_TO_DISPATCH)
                .deploy(rule);

        whenAtCallActivity(DISPATCH)
                .thenReturn("DispatchStatus", SEND_TO_STOP_LIST)
                .thenReturn("DispatchStatus", "Dispatched")
                .deploy(rule);

        whenAtCallActivity(STOP_LIST)
                .alwaysReturn("StopListOutcome", SEND_TO_DRAFT)
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO", PROCESS_INSTANCE_VARS)
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(START);

        verify(TOProcess, times(1))
                .hasCompleted(DATA_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TRIAGE);

        verify(TOProcess, times(5))
                .hasCompleted(STOP_LIST);

        verify(TOProcess, times(5))
                .hasCompleted(DRAFT);

        verify(TOProcess, times(4))
                .hasCompleted(QA);

        verify(TOProcess, times(2))
                .hasCompleted(HOME_SEC);

        verify(TOProcess, times(2))
                .hasCompleted(DISPATCH);

        verify(TOProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(TOProcess, times(1))
                .hasCompleted(END);

        // NOT CALLED CHECK
        verify(TOProcess, times(0))
                .hasCompleted(CAMPAIGN);

        // CASE COMPLETED
        verify(bpmnService).completeCase(any());

    }

    @Test
    public void testAllTransfers() {

        whenAtCallActivity(DATA_INPUT)
                .deploy(rule);

        whenAtCallActivity(TRIAGE)
                .thenReturn(
                        "BusAreaStatus", "Transferred"
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "TriageOutcome", SEND_TO_DRAFT
                )
                .deploy(rule);

        whenAtCallActivity(DRAFT)
                .thenReturn(
                        "BusAreaStatus", "Transferred"
                )
                .thenReturn(
                        "BusAreaStatus", "Confirmed",
                        "DraftStatus", SEND_TO_QA
                )
                .deploy(rule);

        whenAtCallActivity(QA)
                .alwaysReturn("QaStatus", SEND_TO_DISPATCH, "HomeSecInt", "")
                .deploy(rule);

        whenAtCallActivity(DISPATCH)
                .alwaysReturn("DispatchStatus", "Dispatched")
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO", PROCESS_INSTANCE_VARS)
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(START);

        verify(TOProcess, times(1))
                .hasCompleted(DATA_INPUT);

        verify(TOProcess, times(2))
                .hasCompleted(TRIAGE);

        verify(TOProcess, times(2))
                .hasCompleted(DRAFT);

        verify(TOProcess, times(1))
                .hasCompleted(QA);

        verify(TOProcess, times(1))
                .hasCompleted(DISPATCH);

        verify(TOProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(TOProcess, times(1))
                .hasCompleted(END);

        // NOT CALLED CHECK
        verify(TOProcess, times(0))
                .hasCompleted(CAMPAIGN);

        verify(TOProcess, times(0))
                .hasCompleted(STOP_LIST);

        verify(TOProcess, times(0))
                .hasCompleted(HOME_SEC);

        // CASE COMPLETED
        verify(bpmnService).completeCase(any());
    }

    @Test
    public void testAllRejectDraft() {

        whenAtCallActivity(DATA_INPUT)
                .deploy(rule);

        whenAtCallActivity(TRIAGE)
                .alwaysReturn("BusAreaStatus", "Confirmed")
                .alwaysReturn("TriageOutcome", SEND_TO_DRAFT)
                .deploy(rule);

        whenAtCallActivity(DRAFT)
                .alwaysReturn("BusAreaStatus", "Confirmed","DraftStatus", SEND_TO_QA)
                .deploy(rule);

        whenAtCallActivity(QA)
                .thenReturn("QaStatus", SEND_TO_DRAFT)
                .thenReturn("QaStatus", SEND_TO_DISPATCH, "HomeSecInt", "Yes")
                .thenReturn("QaStatus", SEND_TO_DISPATCH, "HomeSecInt", "Yes")
                .deploy(rule);

        whenAtCallActivity(HOME_SEC)
                .thenReturn("HomeSecStatus", REJECT_DRAFT)
                .thenReturn("HomeSecStatus", SEND_TO_DISPATCH)
                .deploy(rule);

        whenAtCallActivity(DISPATCH)
                .alwaysReturn("DispatchStatus", "Dispatched")
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO", PROCESS_INSTANCE_VARS)
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(START);

        verify(TOProcess, times(1))
                .hasCompleted(DATA_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TRIAGE);

        verify(TOProcess, times(3))
                .hasCompleted(DRAFT);

        verify(TOProcess, times(3))
                .hasCompleted(QA);

        verify(TOProcess, times(2))
                .hasCompleted(HOME_SEC);

        verify(TOProcess, times(1))
                .hasCompleted(DISPATCH);

        verify(TOProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(TOProcess, times(1))
                .hasCompleted(END);

        // NOT CALLED CHECK
        verify(TOProcess, times(0))
                .hasCompleted(CAMPAIGN);

        verify(TOProcess, times(0))
                .hasCompleted(STOP_LIST);

        // CASE COMPLETED
        verify(bpmnService).completeCase(any());

    }

    @Test
    public void testAllSendToTriage() {
        whenAtCallActivity(DATA_INPUT)
                .deploy(rule);

        whenAtCallActivity(TRIAGE)
                .thenReturn("BusAreaStatus", "Confirmed", "TriageOutcome", PUT_ON_CAMPAIGN)
                .thenReturn("BusAreaStatus", "Confirmed", "TriageOutcome", SEND_TO_STOP_LIST)
                .thenReturn("BusAreaStatus", "Confirmed", "TriageOutcome", SEND_TO_DRAFT)
                .thenReturn("BusAreaStatus", "Confirmed", "TriageOutcome", SEND_TO_DRAFT)
                .thenReturn("BusAreaStatus", "Confirmed", "TriageOutcome", SEND_TO_DRAFT)
                .deploy(rule);

        whenAtCallActivity(CAMPAIGN)
                .thenReturn("CampaignOutcome", SEND_TO_TRIAGE)
                .deploy(rule);

        whenAtCallActivity(STOP_LIST)
                .thenReturn("StopListOutcome", SEND_TO_TRIAGE)
                .deploy(rule);

        whenAtCallActivity(DRAFT)
                .thenReturn("BusAreaStatus", "Confirmed", "DraftStatus", SEND_TO_TRIAGE)
                .thenReturn("BusAreaStatus", "Confirmed", "DraftStatus", SEND_TO_QA)
                .thenReturn("BusAreaStatus", "Confirmed", "DraftStatus", SEND_TO_QA)
                .deploy(rule);

        whenAtCallActivity(QA)
                .thenReturn("QaStatus", SEND_TO_TRIAGE)
                .thenReturn("QaStatus", SEND_TO_DISPATCH, "HomeSecInt", "")
                .deploy(rule);

        whenAtCallActivity(DISPATCH)
                .alwaysReturn("DispatchStatus", "Dispatched")
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO", PROCESS_INSTANCE_VARS)
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(START);

        verify(TOProcess, times(1))
                .hasCompleted(DATA_INPUT);

        verify(TOProcess, times(5))
                .hasCompleted(TRIAGE);

        verify(TOProcess, times(3))
                .hasCompleted(DRAFT);

        verify(TOProcess, times(2))
                .hasCompleted(QA);

        verify(TOProcess, times(1))
                .hasCompleted(DISPATCH);

        verify(TOProcess, times(1))
                .hasCompleted(COMPLETE_CASE);

        verify(TOProcess, times(1))
                .hasCompleted(END);

        verify(TOProcess, times(1))
                .hasCompleted(CAMPAIGN);

        verify(TOProcess, times(1))
                .hasCompleted(STOP_LIST);

        // NOT CALLED CHECK
        verify(TOProcess, times(0))
                .hasCompleted(HOME_SEC);

        // CASE COMPLETED
        verify(bpmnService).completeCase(any());
    }
}
