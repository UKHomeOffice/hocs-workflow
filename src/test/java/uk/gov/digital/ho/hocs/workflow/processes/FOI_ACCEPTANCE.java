package uk.gov.digital.ho.hocs.workflow.processes;

import static org.mockito.ArgumentMatchers.eq;
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

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/FOI_ACCEPTANCE.bpmn"})
public class FOI_ACCEPTANCE {

    public static final String ACCEPT_OR_REJECT = "ACCEPT_OR_REJECT";
    public static final String DEADLINE_PASSED = "DEADLINE_PASSED";
    public static final String CHOOSE_DRAFT_TEAM = "CHOOSE_DRAFT_TEAM";
    public static final String PROCESS_KEY = "FOI_ACCEPTANCE";
    public static final String CASE_UUID = UUID.randomUUID().toString();
    public static final String STAGE_UUID = UUID.randomUUID().toString();
    public static final String FOI_CASE_TYPE = "FOI";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(1)
            .build();

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
        when(bpmnService.calculateDeadline(eq(FOI_CASE_TYPE), eq(2))).thenReturn(futureDeadline);

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT))
                .thenReturn(TaskDelegate::complete);
        when(processScenario.waitsAtUserTask(CHOOSE_DRAFT_TEAM))
                .thenReturn(TaskDelegate::complete);

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID)
                )).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(CHOOSE_DRAFT_TEAM);
    }

    @Test
    public void deadlinePassed() {

        Date pastDeadline = new GregorianCalendar(2000, Calendar.JANUARY, 1, 0, 0, 0).getTime();
        when(bpmnService.calculateDeadline(eq(FOI_CASE_TYPE), eq(2))).thenReturn(pastDeadline);

        when(processScenario.waitsAtUserTask(DEADLINE_PASSED))
            .thenReturn(TaskDelegate::complete);

        when(processScenario.waitsAtUserTask(CHOOSE_DRAFT_TEAM))
            .thenReturn(TaskDelegate::complete);

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(
                PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID)
            )).execute();

        verify(processScenario, times(1)).hasCanceled(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(DEADLINE_PASSED);
        verify(processScenario, times(1)).hasCompleted(CHOOSE_DRAFT_TEAM);
    }
}
