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
        "processes/TO_DRAFT.bpmn",
        "processes/TO_CHANGE_BUSINESS_AREA.bpmn"
})
public class TO_DRAFT {

    // COMMON GATEWAY &  GATEWAY OUTCOMES
    private static final String DIRECTION = "DIRECTION";
    private static final String DRAFT_STATUS = "DraftStatus";
    private static final String FORWARD = "FORWARD";
    private static final String SAVE = "Save";
    private static final String TO_DISPATCH = "SendToDispatch";
    private static final String CHANGE_BUSINESS_AREA = "ChangeBusinessArea";
    private static final String BACKWARD = "BACKWARD";

    // USER AND SERVICE TASKS
    private static final String TO_DRAFT_UPLOAD_DOC = "TO_DRAFT_UPLOAD_DOC";
    private static final String TO_CHANGE_BUSINESS_AREA = "TO_CHANGE_BUSINESS_AREA";


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
    public void shouldSaveAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_DRAFT_UPLOAD_DOC))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, DRAFT_STATUS, SAVE)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, DRAFT_STATUS, TO_DISPATCH)));


        Scenario.run(TOProcess)
                .startByKey("TO_DRAFT")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_DRAFT_UPLOAD_DOC);
    }

    @Test
    public void shouldChangeBusinessAreaAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_DRAFT_UPLOAD_DOC))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, CHANGE_BUSINESS_AREA)));

        whenAtCallActivity(TO_CHANGE_BUSINESS_AREA)
                .thenReturn("BusAreaStatus", "Transferred", DIRECTION, FORWARD, "TROFTeamUUID", "e4925c53-cbec-4690-a9f0-e09111fb281f")
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO_DRAFT")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_DRAFT_UPLOAD_DOC);

        verify(TOProcess, times(1))
                .hasCompleted(TO_CHANGE_BUSINESS_AREA);
    }

    @Test
    public void shouldChangeBusinessAreaAndGoBackAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_DRAFT_UPLOAD_DOC))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, CHANGE_BUSINESS_AREA)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION,FORWARD,DRAFT_STATUS,TO_DISPATCH)));

        whenAtCallActivity(TO_CHANGE_BUSINESS_AREA)
                .thenReturn(DIRECTION, BACKWARD)
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO_DRAFT")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_DRAFT_UPLOAD_DOC);


        verify(TOProcess, times(1))
                .hasCompleted(TO_CHANGE_BUSINESS_AREA);
    }
}
