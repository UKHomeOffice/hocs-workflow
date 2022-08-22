package uk.gov.digital.ho.hocs.workflow.processes.foi;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
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

import java.util.*;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/FOI/FOI_DRAFT.bpmn"})
public class FOI_DRAFT {

    public static final String PROCESS_KEY = "FOI_DRAFT";
    public static final String CASE_UUID = UUID.randomUUID().toString();
    public static final String STAGE_UUID = UUID.randomUUID().toString();

    public static final String MULTIPLE_CONTRIBUTIONS = "MULTIPLE_CONTRIBUTIONS";
    public static final String END_EVENT = "END_EVENT";
    public static final String ARE_MCS_REQUIRED = "ARE_MCS_REQUIRED";
    public static final String UPLOAD_DRAFT = "UPLOAD_DRAFT";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.75)
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
    public void multipleContributions() {

        when(processScenario.waitsAtUserTask(ARE_MCS_REQUIRED))
                .thenReturn(task -> task.complete(withVariables(
                        "ContributionsRequired", "RequestContrib-Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(MULTIPLE_CONTRIBUTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));


        when(processScenario.waitsAtUserTask(UPLOAD_DRAFT))
                .thenReturn(task -> task.complete());

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID)
                )).execute();

        verify(processScenario, times(1)).hasCompleted(ARE_MCS_REQUIRED);
        verify(processScenario, times(1)).hasCompleted(MULTIPLE_CONTRIBUTIONS);
        verify(processScenario, times(1)).hasCompleted(UPLOAD_DRAFT);
        verify(processScenario).hasFinished(END_EVENT);
    }

    @Test
    public void multipleContributionsBackandForth() {

        when(processScenario.waitsAtUserTask(ARE_MCS_REQUIRED))
                .thenReturn(task -> task.complete(withVariables(
                        "ContributionsRequired", "RequestContrib-N", "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "ContributionsRequired", "RequestContrib-Y", "DIRECTION", "FORWARD")))
                .thenReturn(task -> task.complete(withVariables(
                        "ContributionsRequired", "RequestContrib-Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(MULTIPLE_CONTRIBUTIONS))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));


        when(processScenario.waitsAtUserTask(UPLOAD_DRAFT))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "BACKWARD")))
                .thenReturn(task -> task.complete());

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID)
                )).execute();

        verify(processScenario, times(3)).hasCompleted(ARE_MCS_REQUIRED);
        verify(processScenario, times(2)).hasCompleted(MULTIPLE_CONTRIBUTIONS);
        verify(processScenario, times(2)).hasCompleted(UPLOAD_DRAFT);
        verify(processScenario).hasFinished(END_EVENT);
    }

    @Test
    public void happyPath() {

        when(processScenario.waitsAtUserTask(ARE_MCS_REQUIRED))
                .thenReturn(task -> task.complete(withVariables(
                        "ContributionsRequired", "RequestContrib-N", "DIRECTION", "FORWARD")));


        when(processScenario.waitsAtUserTask(UPLOAD_DRAFT))
                .thenReturn(task -> task.complete());

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID)
                )).execute();

        verify(processScenario, times(1)).hasCompleted(ARE_MCS_REQUIRED);
        verify(processScenario, times(1)).hasCompleted(UPLOAD_DRAFT);
        verify(processScenario).hasFinished(END_EVENT);
        verify(processScenario, never()).waitsAtUserTask(MULTIPLE_CONTRIBUTIONS);
    }
}
