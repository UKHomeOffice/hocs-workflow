package uk.gov.digital.ho.hocs.workflow.processes.to;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
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
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/TO_DISPATCH.bpmn"
})
public class TO_DISPATCH {

    private static final String SAVE = "Save";
    private static final String DISPATCHED = "Dispatched";
    private static final String CAMPAIGN = "PutOnCampaign";
    private static final String STOP_LIST = "SendToStopList";
    private static final String DISPATCH_STATUS = "DispatchStatus";
    private static final String DIRECTION = "DIRECTION";
    private static final String BACKWARD = "BACKWARD";
    private static final String FORWARD = "FORWARD";

    // USER AND SERVICE TASKS
    private static final String TO_DISPATCH_FINAL_RESPONSE = "TO_DISPATCH_FINAL_RESPONSE";
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
    public void testSaveOnceThenCompleteCase() {

        when(TOProcess.waitsAtUserTask(TO_DISPATCH_FINAL_RESPONSE))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,SAVE)))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,DISPATCHED)));

        Scenario.run(TOProcess)
                .startByKey("TO_DISPATCH")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_DISPATCH_FINAL_RESPONSE);

        // NOT CALLED PROCESSES
        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_CAMPAIGN_TYPE);
    }

    @Test
    public void testSaveOnceThenPutOnCampaignWithDefaultFlow() {

        when(TOProcess.waitsAtUserTask(TO_DISPATCH_FINAL_RESPONSE))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,SAVE)))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,CAMPAIGN)));

        when(TOProcess.waitsAtUserTask(TO_GET_CAMPAIGN_TYPE))
                .thenReturn(taskDelegate -> taskDelegate.complete(withVariables(DIRECTION, FORWARD)));

        Scenario.run(TOProcess)
                .startByKey("TO_DISPATCH")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_DISPATCH_FINAL_RESPONSE);

    }

    @Test
    public void testSaveOnceThenPutOnCampaignBackAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_DISPATCH_FINAL_RESPONSE))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,CAMPAIGN)))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,DISPATCHED)));

        when(TOProcess.waitsAtUserTask(TO_GET_CAMPAIGN_TYPE))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, BACKWARD)));

        Scenario.run(TOProcess)
                .startByKey("TO_DISPATCH")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_DISPATCH_FINAL_RESPONSE);

    }

    @Test
    public void testSaveOnceThenSendToStopList() {

        when(TOProcess.waitsAtUserTask(TO_DISPATCH_FINAL_RESPONSE))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,SAVE)))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,STOP_LIST)));

        Scenario.run(TOProcess)
                .startByKey("TO_DISPATCH")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_DISPATCH_FINAL_RESPONSE);


        // NOT CALLED PROCESSES

        verify(TOProcess, times(0))
                .hasCompleted(TO_GET_CAMPAIGN_TYPE);
    }
}
