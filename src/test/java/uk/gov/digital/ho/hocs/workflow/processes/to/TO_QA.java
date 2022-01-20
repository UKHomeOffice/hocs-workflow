package uk.gov.digital.ho.hocs.workflow.processes.to;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.model.bpmn.impl.instance.To;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.support.PropertySourceFactory;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.task;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/TO_QA.bpmn"
})
public class TO_QA {

    // COMMON GATEWAY &  GATEWAY OUTCOMES
    private static final String DIRECTION = "DIRECTION";
    private static final String QA_STATUS = "QaStatus";
    private static final String BACKWARD = "BACKWARD";
    private static final String FORWARD = "FORWARD";
    private static final String PUT_ON_CAMPAIGN = "PutOnCampaign";
    private static final String CHANGE_BUSINESS_AREA = "ChangeBusinessArea";
    private static final String TO_CHANGE_BUSINESS_AREA = "TO_CHANGE_BUSINESS_AREA";
    private static final String UPDATE_BUS_AREA_STATUS = "UPDATE_BUS_AREA_STATUS";

    // USER AND SERVICE TASKS
    private static final String TO_QA_OUTCOME = "TO_QA_OUTCOME";
    private static final String TO_ENQUIRY_SUBJECT_REASON = "TO_ENQUIRY_SUBJECT_REASON";
    private static final String TO_QA_REJECTION_NOTE = "TO_QA_REJECTION_NOTE";
    private static final String UPDATE_APP_DecisionNotYetMade = "Activity_0xe7z8s";
    private static final String UPDATE_APP_Approved = "Activity_1e5czwh";
    private static final String UPDATE_APP_Rejected = "Activity_0k5qv30";
    private static final String CLEAR_REJ_NOTE = "Activity_0extn6g";
    private static final String SAVE_REJ_NOTE = "Activity_1t508ui";
    private static final String TO_GET_CAMPAIGN_TYPE = "TO_GET_CAMPAIGN_TYPE";

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
    public void testSaveOnceThenAccept() {

        when(TOProcess.waitsAtUserTask(TO_QA_OUTCOME))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS, "Save", DIRECTION, FORWARD)))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToDispatch", DIRECTION, FORWARD)));

        Scenario.run(TOProcess)
                .startByKey("TO_QA")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_QA_OUTCOME);

        verify(TOProcess, times(1))
                .hasCompleted(CLEAR_REJ_NOTE);

        verify(TOProcess, times(1))
                .hasCompleted(UPDATE_APP_Approved);

        // Not invoked
        verify(TOProcess, times(0))
                .hasCompleted(TO_QA_REJECTION_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJ_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_Rejected);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_DecisionNotYetMade);
    }


    @Test
    public void shouldChangeBusinessAreaAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_QA_OUTCOME))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, CHANGE_BUSINESS_AREA)));

        whenAtCallActivity(TO_CHANGE_BUSINESS_AREA)
                .thenReturn("BusAreaStatus", "Transferred", DIRECTION, FORWARD, "TROFTeamUUID", "e4925c53-cbec-4690-a9f0-e09111fb281f")
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO_QA")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_QA_OUTCOME);

        verify(TOProcess, times(0))
                .hasCompleted(TO_ENQUIRY_SUBJECT_REASON);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_BUS_AREA_STATUS);

        verify(TOProcess, times(1))
                .hasCompleted(TO_CHANGE_BUSINESS_AREA);
    }

    @Test
    public void testRejectSendToDraft() {
        when(TOProcess.waitsAtUserTask(TO_QA_OUTCOME))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToDraft", DIRECTION, FORWARD)));

        when(TOProcess.waitsAtUserTask(TO_QA_REJECTION_NOTE))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD)));

        Scenario.run(TOProcess)
                .startByKey("TO_QA")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_QA_OUTCOME);

        verify(TOProcess, times(1))
                .hasCompleted(TO_QA_REJECTION_NOTE);

        verify(TOProcess, times(1))
                .hasCompleted(SAVE_REJ_NOTE);

        verify(TOProcess, times(1))
                .hasCompleted(UPDATE_APP_Rejected);

        // Not invoked
        verify(TOProcess, times(0))
                .hasCompleted(CLEAR_REJ_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_Approved);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_DecisionNotYetMade);

    }

    @Test
    public void testRejectSendToTriageGoBackSentToTriage() {

        when(TOProcess.waitsAtUserTask(TO_QA_OUTCOME))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToDraft", DIRECTION, FORWARD)))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToTriage", DIRECTION, FORWARD)));

        when(TOProcess.waitsAtUserTask(TO_QA_REJECTION_NOTE))
                .thenReturn(task -> task.complete(withVariables(DIRECTION,BACKWARD)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD)));

        Scenario.run(TOProcess)
                .startByKey("TO_QA")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_QA_OUTCOME);

        verify(TOProcess, times(2))
                .hasCompleted(TO_QA_REJECTION_NOTE);

        verify(TOProcess, times(1))
                .hasCompleted(SAVE_REJ_NOTE);

        verify(TOProcess, times(1))
                .hasCompleted(UPDATE_APP_Rejected);

        // Not invoked
        verify(TOProcess, times(0))
                .hasCompleted(CLEAR_REJ_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_Approved);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_DecisionNotYetMade);
    }

    @Test
    public void testPutOnStopList() {
        when(TOProcess.waitsAtUserTask(TO_QA_OUTCOME))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToStopList", DIRECTION, FORWARD)));

        Scenario.run(TOProcess)
                .startByKey("TO_QA")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_QA_OUTCOME);

        verify(TOProcess, times(1))
                .hasCompleted(UPDATE_APP_DecisionNotYetMade);

        // Not invoked
        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJ_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_Rejected);

        verify(TOProcess, times(0))
                .hasCompleted(CLEAR_REJ_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_Approved);

        verify(TOProcess, times(0))
                .hasCompleted(TO_QA_REJECTION_NOTE);
    }

    @Test
    public void testPutOnCampaign() {
        when(TOProcess.waitsAtUserTask(TO_QA_OUTCOME))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS, PUT_ON_CAMPAIGN, DIRECTION, FORWARD)));

        when(TOProcess.waitsAtUserTask(TO_GET_CAMPAIGN_TYPE)).thenReturn(
                task -> task.complete(withVariables(DIRECTION,FORWARD)));

        Scenario.run(TOProcess)
                .startByKey("TO_QA")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_QA_OUTCOME);

        verify(TOProcess, times(1))
                .hasCompleted(UPDATE_APP_DecisionNotYetMade);

        // Not invoked
        verify(TOProcess, times(0))
                .hasCompleted(SAVE_REJ_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_Rejected);

        verify(TOProcess, times(0))
                .hasCompleted(CLEAR_REJ_NOTE);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_APP_Approved);

        verify(TOProcess, times(0))
                .hasCompleted(TO_QA_REJECTION_NOTE);
    }
}
