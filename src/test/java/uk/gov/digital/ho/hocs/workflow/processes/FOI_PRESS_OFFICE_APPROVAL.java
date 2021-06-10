package uk.gov.digital.ho.hocs.workflow.processes;

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

import java.util.Map;
import java.util.UUID;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/FOI_PRESS_OFFICE_APPROVAL.bpmn"})
public class FOI_PRESS_OFFICE_APPROVAL {

    public static final String PROCESS_KEY = "FOI_PRESS_OFFICE_APPROVAL";
    public static final String CASE_UUID = UUID.randomUUID().toString();
    public static final String STAGE_UUID = UUID.randomUUID().toString();
    public static final String DRAFT_TEAM_UUID = UUID.randomUUID().toString();

    public static final String PO_APPROVAL = "PO_APPROVAL";
    public static final String PPO_APPROVAL= "PPO_APPROVAL";
    public static final String SAVE_APPROVAL_NOTE_CONDITION= "SAVE_APPROVAL_NOTE_CONDITION";
    public static final String SAVE_PO_APPROVAL_NOTE= "SAVE_PO_APPROVAL_NOTE";
    public static final String END_EVENT = "END_EVENT";


    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
           // .assertClassCoverageAtLeast(0.75)
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
    public void happyPath_low_sensitivity() {

        when(processScenario.waitsAtUserTask(PO_APPROVAL))
                .thenReturn(task -> task.complete(withVariables(
                        "POApproval", "POAPPROVE")));

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID, "Sensitivity", "LOW",
                                "DraftTeam", DRAFT_TEAM_UUID)
                )).execute();

        verify(processScenario, times(1)).hasCompleted(PO_APPROVAL);
        verify(processScenario, times(1)).hasCompleted(SAVE_APPROVAL_NOTE_CONDITION);
        verify(processScenario, times(1)).hasCompleted(END_EVENT);
    }

    @Test
    public void happyPath_low_sensitivity_with_approval_notes() {

        when(processScenario.waitsAtUserTask(PO_APPROVAL))
                .thenReturn(task -> task.complete(withVariables(
                        "POApproval", "POAPPROVEWITHNOTES")));

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID, "Sensitivity", "LOW",
                                "DraftTeam", DRAFT_TEAM_UUID)
                )).execute();

        verify(processScenario, times(1)).hasCompleted(PO_APPROVAL);
        verify(processScenario, times(1)).hasCompleted(SAVE_APPROVAL_NOTE_CONDITION);
        verify(processScenario, times(1)).hasCompleted(SAVE_PO_APPROVAL_NOTE);
        verify(processScenario, times(1)).hasCompleted(END_EVENT);
    }

    @Test
    public void happyPath_high_sensitivity() {

        when(processScenario.waitsAtUserTask(PPO_APPROVAL))
                .thenReturn(task -> task.complete());

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID, "Sensitivity", "HIGH",
                                "DraftTeam", DRAFT_TEAM_UUID)
                )).execute();

        verify(processScenario, times(1)).hasCompleted(PPO_APPROVAL);
        verify(processScenario, times(1)).hasCompleted(END_EVENT);
    }
}
