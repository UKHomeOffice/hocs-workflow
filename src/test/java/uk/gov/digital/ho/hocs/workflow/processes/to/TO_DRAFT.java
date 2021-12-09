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
        "processes/TO_DRAFT.bpmn",
})
public class TO_DRAFT {

    // COMMON GATEWAY &  GATEWAY OUTCOMES
    private static final String DIRECTION = "DIRECTION";
    private static final String DRAFT_STATUS = "DraftStatus";
    private static final String FORWARD = "FORWARD";
    private static final String SAVE = "Save";
    private static final String TO_DISPATCH = "SendToDispatch";

    // USER AND SERVICE TASKS
    private static final String TO_DRAFT_UPLOAD_DOC = "TO_DRAFT_UPLOAD_DOC";

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
    public void shouldAndSaveAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_DRAFT_UPLOAD_DOC))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, DRAFT_STATUS, SAVE)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, DRAFT_STATUS, TO_DISPATCH)));


        Scenario.run(TOProcess)
                .startByKey("TO_DRAFT")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_DRAFT_UPLOAD_DOC);
    }
}