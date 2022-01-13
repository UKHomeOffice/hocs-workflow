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

    // USER AND SERVICE TASKS
    private static final String TO_DISPATCH_FINAL_RESPONSE = "TO_DISPATCH_FINAL_RESPONSE";

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

    }

    @Test
    public void testSaveOnceThenPutOnCampaign() {

        when(TOProcess.waitsAtUserTask(TO_DISPATCH_FINAL_RESPONSE))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,SAVE)))
                .thenReturn(task -> task.complete(withVariables(DISPATCH_STATUS,CAMPAIGN)));

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

    }
}
