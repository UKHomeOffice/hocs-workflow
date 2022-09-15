package uk.gov.digital.ho.hocs.workflow.processes.foi;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

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

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/FOI/FOI_ALLOCATION.bpmn" })
public class FOI_ALLOCATION {

    public static final String CHOOSE_FOI_HUB = "Activity_0egyc0f";

    public static final String CONFIRMATION_SCREEN = "Activity_0gpqsvz";

    public static final String CASE_UUID = "123-456-789";

    public static final String STAGE_UUID = "987-654-321";

    public static final String PROCESS_KEY = "FOI_ALLOCATION";

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

        //given
        when(processScenario.waitsAtUserTask(CHOOSE_FOI_HUB)).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));
        when(processScenario.waitsAtUserTask(CONFIRMATION_SCREEN)).thenReturn(task -> task.complete());

        //when
        Scenario.run(processScenario).startBy(() -> {
            return rule.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID, "AcceptanceTeam", ACCEPTANCE_TEAM_UUID, "StageUUID", STAGE_UUID));
        }).execute();

        //then
        verify(processScenario, times(1)).hasCompleted(CHOOSE_FOI_HUB);
        verify(processScenario, times(1)).hasCompleted(CONFIRMATION_SCREEN);
    }

    @Test
    public void backPath() {

        //given
        when(processScenario.waitsAtUserTask(CHOOSE_FOI_HUB)).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "BACKWARD"))).thenReturn(
            task -> task.complete(withVariables("DIRECTION", "FORWARD")));
        when(processScenario.waitsAtUserTask(CONFIRMATION_SCREEN)).thenReturn(task -> task.complete());

        //when
        Scenario.run(processScenario).startBy(() -> {
            return rule.getRuntimeService().startProcessInstanceByKey(PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID, "AcceptanceTeam", ACCEPTANCE_TEAM_UUID, "StageUUID", STAGE_UUID));
        }).execute();

        //then
        verify(processScenario, times(2)).hasCompleted(CHOOSE_FOI_HUB);
        verify(processScenario, times(2)).hasCompleted(CONFIRMATION_SCREEN);
    }

}
