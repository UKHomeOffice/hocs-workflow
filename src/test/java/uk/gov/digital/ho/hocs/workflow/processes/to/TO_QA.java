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

    // USER AND SERVICE TASKS
    private static final String TO_QA_OUTCOME = "TO_QA_OUTCOME";
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
                .thenReturn(task -> task.complete(withVariables(QA_STATUS, "Save")))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToDispatch")));

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
    public void testRejectSendToDraft() {
        when(TOProcess.waitsAtUserTask(TO_QA_OUTCOME))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToDraft")));

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
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToDraft")))
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToTriage")));

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
                .thenReturn(task -> task.complete(withVariables(QA_STATUS,"SendToStopList")));

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
                .thenReturn(task -> task.complete(withVariables(QA_STATUS, PUT_ON_CAMPAIGN)));

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
