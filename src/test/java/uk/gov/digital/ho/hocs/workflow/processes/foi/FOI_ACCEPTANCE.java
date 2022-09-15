package uk.gov.digital.ho.hocs.workflow.processes.foi;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
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

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/FOI/FOI_ACCEPTANCE.bpmn" })
public class FOI_ACCEPTANCE {

    public static final String ACCEPT_OR_REJECT = "ACCEPT_OR_REJECT";

    public static final String DEADLINE_PASSED = "DEADLINE_PASSED";

    public static final String CHOOSE_DRAFT_TEAM = "CHOOSE_DRAFT_TEAM";

    public static final String CHOOSE_DRAFT_TEAM_NO_BACK = "CHOOSE_DRAFT_TEAM_NO_BACK";

    public static final String REJECT_CASE = "REJECT_CASE";

    public static final String PROCESS_KEY = "FOI_ACCEPTANCE";

    public static final String SAVE_ALLOCATION_NOTE = "SAVE_ALLOCATION_NOTE";

    public static final String CASE_UUID = UUID.randomUUID().toString();

    public static final String STAGE_UUID = UUID.randomUUID().toString();

    public static final String DRAFT_TEAM = UUID.randomUUID().toString();

    public static final String FOI_CASE_TYPE = "FOI";

    public static final String CLEAR_REJECTED = "CLEAR_REJECTED";

    public static final String SET_TO_REJECTED_BY_ACCEPTANCE = "SET_TO_REJECTED_BY_ACCEPTANCE";

    public static final String SET_ACCEPTANCE_DATE = "SET_ACCEPTANCE_DATE";

    public static final String SET_ACCEPTANCE_DATE_AFTER_DEADLINE_PASSED = "SET_ACCEPTANCE_DATE_AFTER_DEADLINE_PASSED";

    public static final String SET_ACCEPTED = "SET_ACCEPTED";

    public static final String N_TEXT = "NoteText";

    public static final String ACCEPTANCE_TEAM_UUID = UUID.randomUUID().toString();

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(
        1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void happyPath() {

        Date futureDeadline = new GregorianCalendar(3000, Calendar.JANUARY, 1, 0, 0, 0).getTime();
        when(bpmnService.calculateDeadline(eq(FOI_CASE_TYPE), eq(3))).thenReturn(futureDeadline);

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT)).thenReturn(
            task -> task.complete(withVariables("AcceptCase", "Y")));

        when(processScenario.waitsAtUserTask(CHOOSE_DRAFT_TEAM)).thenReturn(
            task -> task.complete(withVariables("DraftTeam", DRAFT_TEAM, "DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID))).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(SET_ACCEPTANCE_DATE);
        verify(processScenario, times(1)).hasCompleted(CHOOSE_DRAFT_TEAM);
        verify(processScenario, times(1)).hasCompleted(CLEAR_REJECTED);
    }

    @Test
    public void acceptedAfterClickingBackPath() {

        Date futureDeadline = new GregorianCalendar(3000, Calendar.JANUARY, 1, 0, 0, 0).getTime();
        when(bpmnService.calculateDeadline(eq(FOI_CASE_TYPE), eq(3))).thenReturn(futureDeadline);

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT)).thenReturn(
            task -> task.complete(withVariables("AcceptCase", "Y")));

        when(processScenario.waitsAtUserTask(CHOOSE_DRAFT_TEAM)).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DraftTeam", DRAFT_TEAM, "DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID))).execute();

        verify(processScenario, times(2)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(2)).hasCompleted(SET_ACCEPTANCE_DATE);
        verify(processScenario, times(2)).hasCompleted(CHOOSE_DRAFT_TEAM);
        verify(processScenario, times(1)).hasCompleted(CLEAR_REJECTED);
    }

    @Test
    public void deadlinePassed() {

        Date pastDeadline = new GregorianCalendar(2000, Calendar.JANUARY, 1, 0, 0, 0).getTime();
        when(bpmnService.calculateDeadline(eq(FOI_CASE_TYPE), eq(3))).thenReturn(pastDeadline);

        when(processScenario.waitsAtUserTask(DEADLINE_PASSED)).thenReturn(TaskDelegate::complete);

        when(processScenario.waitsAtUserTask(CHOOSE_DRAFT_TEAM_NO_BACK)).thenReturn(
            task -> task.complete(withVariables("DraftTeam", DRAFT_TEAM)));

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID))).execute();

        verify(processScenario, times(1)).hasCanceled(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(DEADLINE_PASSED);
        verify(processScenario, times(1)).hasCompleted(CHOOSE_DRAFT_TEAM_NO_BACK);
        verify(processScenario, times(1)).hasCompleted(SET_ACCEPTED);
        verify(processScenario, times(1)).hasCompleted(SET_ACCEPTANCE_DATE_AFTER_DEADLINE_PASSED);
        verify(processScenario, times(1)).hasCompleted(CLEAR_REJECTED);
    }

    @Test
    public void caseHasBeenReturnedFromDraftAcceptance() {

        when(processScenario.waitsAtUserTask(CHOOSE_DRAFT_TEAM_NO_BACK)).thenReturn(
            task -> task.complete(withVariables("DraftTeam", DRAFT_TEAM)));

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID, "AcceptanceDate", "1619779067602"))).execute();

        verify(processScenario, times(1)).hasCompleted(CHOOSE_DRAFT_TEAM_NO_BACK);
        verify(processScenario, times(0)).hasCompleted(SET_ACCEPTANCE_DATE);
    }

    @Test
    public void caseRejected() {
        Date futureDeadline = new GregorianCalendar(3000, Calendar.JANUARY, 1, 0, 0, 0).getTime();
        when(bpmnService.calculateDeadline(eq(FOI_CASE_TYPE), eq(3))).thenReturn(futureDeadline);

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT)).thenReturn(
            task -> task.complete(withVariables("AcceptCase", "N")));

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID, "AcceptanceTeam", ACCEPTANCE_TEAM_UUID, "NText", N_TEXT, "StageUUID",
                    STAGE_UUID))).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(REJECT_CASE);
        verify(processScenario, times(1)).hasCompleted(SAVE_ALLOCATION_NOTE);
        verify(processScenario, times(1)).hasCompleted(SET_TO_REJECTED_BY_ACCEPTANCE);
        verify(processScenario, times(0)).hasCompleted(SET_ACCEPTANCE_DATE);
        verify(bpmnService).wipeVariables(eq(CASE_UUID), eq(STAGE_UUID), eq("AcceptanceTeam"), eq("Directorate"));
        verify(bpmnService).updateAllocationNoteWithDetails(eq(CASE_UUID), eq(STAGE_UUID), eq(N_TEXT), eq("REJECT"),
            eq(ACCEPTANCE_TEAM_UUID), eq(STAGE_UUID));
    }

}
