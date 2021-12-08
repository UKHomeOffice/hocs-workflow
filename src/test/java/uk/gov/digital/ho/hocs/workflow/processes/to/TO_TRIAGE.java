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

import java.util.UUID;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/TO_TRIAGE.bpmn",
        "processes/TO_ENQUIRY_SUBJECT_REASON.bpmn",
        "processes/TO_CHANGE_BUSINESS_AREA.bpmn"
})
public class TO_TRIAGE {

    // COMMON GATEWAY &  GATEWAY OUTCOMES
    private static final String DIRECTION = "DIRECTION";
    private static final String TRIAGE_OUTCOME = "TriageOutcome";
    private static final String BACKWARD = "BACKWARD";
    private static final String FORWARD = "FORWARD";
    private static final String SET_ENQUIRY = "SetEnquiry";
    private static final String CHANGE_BUSINESS_AREA = "ChangeBusinessArea";
    private static final String SAVE = "Save";
    private static final String TO_DRAFT = "SendToDraft";

    // USER AND SERVICE TASKS
    private static final String TO_TRIAGE_INPUT = "TO_TRIAGE_INPUT";
    private static final String UPDATE_BUS_AREA_STATUS = "Activity_0y2w7dl";
    private static final String TO_ENQUIRY_SUBJECT_REASON = "TO_ENQUIRY_SUBJECT_REASON";
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
    public void shouldSetEnquiryAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_TRIAGE_INPUT))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, SET_ENQUIRY)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, TRIAGE_OUTCOME, TO_DRAFT)));

        whenAtCallActivity(TO_ENQUIRY_SUBJECT_REASON)
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO_TRIAGE")
                .execute();

        verify(TOProcess, times(2))
                .hasCompleted(TO_TRIAGE_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TO_ENQUIRY_SUBJECT_REASON);

        verify(TOProcess, times(1))
                .hasCompleted(UPDATE_BUS_AREA_STATUS);

        verify(TOProcess, times(0))
                .hasCompleted(TO_CHANGE_BUSINESS_AREA);
    }

    @Test
    public void shouldSetEnquiryAndSaveAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_TRIAGE_INPUT))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, SET_ENQUIRY)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, TRIAGE_OUTCOME, SAVE)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, FORWARD, TRIAGE_OUTCOME, TO_DRAFT)));

        whenAtCallActivity(TO_ENQUIRY_SUBJECT_REASON).deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO_TRIAGE")
                .execute();

        verify(TOProcess, times(3))
                .hasCompleted(TO_TRIAGE_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TO_ENQUIRY_SUBJECT_REASON);

        verify(TOProcess, times(1))
                .hasCompleted(UPDATE_BUS_AREA_STATUS);

        verify(TOProcess, times(0))
                .hasCompleted(TO_CHANGE_BUSINESS_AREA);
    }

    @Test
    public void shouldChangeBusinessAreaAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_TRIAGE_INPUT))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, CHANGE_BUSINESS_AREA)));

        whenAtCallActivity(TO_CHANGE_BUSINESS_AREA)
                .thenReturn("BusAreaStatus", "Transferred", DIRECTION, FORWARD, "TROFTeamUUID", UUID.randomUUID().toString())
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO_TRIAGE")
                .execute();

        verify(TOProcess, times(1))
                .hasCompleted(TO_TRIAGE_INPUT);

        verify(TOProcess, times(0))
                .hasCompleted(TO_ENQUIRY_SUBJECT_REASON);

        verify(TOProcess, times(0))
                .hasCompleted(UPDATE_BUS_AREA_STATUS);

        verify(TOProcess, times(1))
                .hasCompleted(TO_CHANGE_BUSINESS_AREA);
    }

    @Test
    public void shouldChangeBusinessAreaAndGoBackAndSetEnquiryAndComplete() {

        when(TOProcess.waitsAtUserTask(TO_TRIAGE_INPUT))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, CHANGE_BUSINESS_AREA)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION, SET_ENQUIRY)))
                .thenReturn(task -> task.complete(withVariables(DIRECTION,FORWARD,TRIAGE_OUTCOME,TO_DRAFT)));

        whenAtCallActivity(TO_CHANGE_BUSINESS_AREA)
                .thenReturn(DIRECTION, BACKWARD)
                .deploy(rule);

        whenAtCallActivity(TO_ENQUIRY_SUBJECT_REASON)
                .deploy(rule);

        Scenario.run(TOProcess)
                .startByKey("TO_TRIAGE")
                .execute();

        verify(TOProcess, times(3))
                .hasCompleted(TO_TRIAGE_INPUT);

        verify(TOProcess, times(1))
                .hasCompleted(TO_ENQUIRY_SUBJECT_REASON);

        verify(TOProcess, times(1))
                .hasCompleted(UPDATE_BUS_AREA_STATUS);

        verify(TOProcess, times(1))
                .hasCompleted(TO_CHANGE_BUSINESS_AREA);
    }
}
