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
@Deployment(resources = {"processes/FOI_QA.bpmn"})
public class FOI_QA {

    public static final String PROCESS_KEY = "FOI_QA";
    public static final String CASE_UUID = UUID.randomUUID().toString();
    public static final String STAGE_UUID = UUID.randomUUID().toString();
    public static final String DRAFT_TEAM_UUID = UUID.randomUUID().toString();

    public static final String SET_APPROVAL_TEAM_MEMBER_FOR_QA_STAGE = "SET_APPROVAL_TEAM_MEMBER_FOR_QA_STAGE";
    public static final String ACCEPT_OR_REJECT_CASE_G6_OR_G7 = "ACCEPT_OR_REJECT_CASE_G6_OR_G7";
    public static final String SAVE_ALLOCATION_NOTE = "SAVE_ALLOCATION_NOTE";
    public static final String REJECT_CASE = "REJECT_CASE";
    public static final String ALLOCATE_TO_DRAFT_TEAM = "ALLOCATE_TO_DRAFT_TEAM";
    public static final String ACCEPT_OR_REJECT_SENSITIVITY_G6_OR_G7 = "ACCEPT_OR_REJECT_SENSITIVITY_G6_OR_G7";
    public static final String APPROVAL_G6_OR_G7 = "APPROVAL_G6_OR_G7";
    public static final String ALLOCATE_TO_PRIVATE_PRESS_OFFICE_TEAM = "ALLOCATE_TO_PPO_TEAM";
    public static final String ALLOCATE_TO_PRESS_OFFICE_TEAM = "ALLOCATE_TO_PO_TEAM";
    public static final String END_EVENT = "END_EVENT";

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
    public void happyPath_low_sensitivity() {

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT_CASE_G6_OR_G7))
                .thenReturn(task -> task.complete(withVariables(
                        "G6orG7AcceptCase", "G6orG7AcceptCase-Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT_SENSITIVITY_G6_OR_G7))
                .thenReturn(task -> task.complete(withVariables(
                        "G6orG7AcceptSensitivityLevel", "G6orG7AcceptSensitivityLevel-Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(APPROVAL_G6_OR_G7))
                .thenReturn(task -> task.complete(withVariables(
                        "G6orG7Approval", "G6orG7Approval-Y", "DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID, "Sensitivity", "LOW",
                                "DraftTeam", DRAFT_TEAM_UUID)
                )).execute();

        verify(processScenario, times(1)).hasCompleted(SET_APPROVAL_TEAM_MEMBER_FOR_QA_STAGE);
        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT_CASE_G6_OR_G7);
        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT_SENSITIVITY_G6_OR_G7);
        verify(processScenario, times(1)).hasCompleted(APPROVAL_G6_OR_G7);
        verify(processScenario, times(1)).hasCompleted(ALLOCATE_TO_PRESS_OFFICE_TEAM);
        verify(processScenario, times(1)).hasCompleted(END_EVENT);
    }


    @Test
    public void caseRejectedAtAcceptance_low_sensitivity() {

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT_CASE_G6_OR_G7))
                .thenReturn(task -> task.complete(withVariables(
                        "G6orG7AcceptCase", "G6orG7AcceptCase-N", "DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID, "Sensitivity", "LOW",
                                "DraftTeam", DRAFT_TEAM_UUID))
        ).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT_CASE_G6_OR_G7);
        verify(processScenario, times(1)).hasCompleted(SAVE_ALLOCATION_NOTE);
        verify(processScenario, times(1)).hasCompleted(REJECT_CASE);
        verify(processScenario, times(1)).hasCompleted(ALLOCATE_TO_DRAFT_TEAM);
        verify(processScenario, times(1)).hasCompleted(END_EVENT);
    }

    @Test
    public void caseRejectedAtSensitivityAcceptance_low_sensitivity() {

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT_CASE_G6_OR_G7))
                .thenReturn(task -> task.complete(withVariables(
                        "G6orG7AcceptCase", "G6orG7AcceptCase-Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT_SENSITIVITY_G6_OR_G7))
                .thenReturn(task -> task.complete(withVariables(
                        "G6orG7AcceptSensitivityLevel", "G6orG7AcceptSensitivityLevel-N", "DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID, "Sensitivity", "LOW",
                                "DraftTeam", DRAFT_TEAM_UUID))
        ).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT_CASE_G6_OR_G7);
        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT_SENSITIVITY_G6_OR_G7);
        verify(processScenario, times(1)).hasCompleted(REJECT_CASE);
        verify(processScenario, times(1)).hasCompleted(ALLOCATE_TO_DRAFT_TEAM);
        verify(processScenario, times(1)).hasCompleted(END_EVENT);

    }

    @Test
    public void happyPath_high_sensitivity() {
        Scenario.run(processScenario).startBy(
                () -> rule.getRuntimeService().startProcessInstanceByKey(
                        PROCESS_KEY, STAGE_UUID,
                        Map.of("CaseUUID", CASE_UUID, "Sensitivity", "HIGH",
                                "DraftTeam", DRAFT_TEAM_UUID)
                )).execute();

        verify(processScenario, times(1)).hasCompleted(ALLOCATE_TO_PRIVATE_PRESS_OFFICE_TEAM);

        verify(processScenario, times(1)).hasCompleted(END_EVENT);
    }
}
