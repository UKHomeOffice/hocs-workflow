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

import java.util.*;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {"processes/FOI_DRAFT.bpmn"})
public class FOI_DRAFT {

    public static final String PROCESS_KEY = "FOI_DRAFT";
    public static final String CASE_UUID = UUID.randomUUID().toString();
    public static final String STAGE_UUID = UUID.randomUUID().toString();
    public static final String ACCEPTANCE_TEAM_UUID = UUID.randomUUID().toString();

    public static final String ACCEPT_OR_REJECT = "ACCEPT_OR_REJECT";
    public static final String REJECT_CASE = "REJECT_CASE";
    public static final String ALLOCATE_TO_ACCEPTANCE_TEAM = "ALLOCATE_TO_ACCEPTANCE_TEAM";
    public static final String SAVE_ALLOCATION_NOTE = "SAVE_ALLOCATION_NOTE";
    public static final String MULTIPLE_CONTRIBUTIONS = "MULTIPLE_CONTRIBUTIONS";
    public static final String END_EVENT = "END_EVENT";
    public static final String VALIDITY = "VALIDITY";
    public static final String ARE_MCS_REQUIRED = "ARE_MCS_REQUIRED";
    public static final String RESPONSE_TYPE = "RESPONSE_TYPE";
    public static final String INVALID = "INVALID";
    public static final String UPLOAD_DRAFT = "UPLOAD_DRAFT";
    public static final String EXEMPTION = "EXEMPTION";
    public static final String SENSITIVITY = "SENSITIVITY";

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
    public void caseRejected() {
        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT))
            .thenReturn(task -> task.complete(withVariables(
                "DraftAcceptCase", "N")));


        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(
                PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID, "AcceptanceTeam", ACCEPTANCE_TEAM_UUID)
            )).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(REJECT_CASE);
        verify(processScenario, times(1)).hasCompleted(ALLOCATE_TO_ACCEPTANCE_TEAM);
        verify(processScenario, times(1)).hasCompleted(SAVE_ALLOCATION_NOTE);
        verify(bpmnService).wipeVariables(eq(CASE_UUID), eq(STAGE_UUID), eq("DraftTeam"));
        verify(processScenario).hasFinished(END_EVENT);

    }

    @Test
    public void invalidRequest() {

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT))
            .thenReturn(task -> task.complete(withVariables(
                "DraftAcceptCase", "Y")));

        when(processScenario.waitsAtUserTask(VALIDITY))
            .thenReturn(task -> task.complete(withVariables(
                "DraftValidity", "N", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(INVALID))
            .thenReturn(task -> task.complete(withVariables(
                "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(UPLOAD_DRAFT))
            .thenReturn(task -> task.complete());

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(
                PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID)
            )).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(VALIDITY);
        verify(processScenario, times(1)).hasCompleted(INVALID);
        verify(processScenario, times(1)).hasCompleted(UPLOAD_DRAFT);
        verify(processScenario).hasFinished(END_EVENT);
        verify(processScenario, never()).waitsAtUserTask(ARE_MCS_REQUIRED);
        verify(processScenario, never()).waitsAtUserTask(SENSITIVITY);
    }

    @Test
    public void multipleContributions() {

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT))
            .thenReturn(task -> task.complete(withVariables(
                "DraftAcceptCase", "Y")));

        when(processScenario.waitsAtUserTask(VALIDITY))
            .thenReturn(task -> task.complete(withVariables(
                "DraftValidity", "Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(ARE_MCS_REQUIRED))
            .thenReturn(task -> task.complete(withVariables(
                "ContributionsRequired", "Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(MULTIPLE_CONTRIBUTIONS))
            .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(RESPONSE_TYPE))
            .thenReturn(task -> task.complete(withVariables(
                "ResponseType", "FULL_DISCLOSURE", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(UPLOAD_DRAFT))
            .thenReturn(task -> task.complete());

        when(processScenario.waitsAtUserTask(SENSITIVITY))
            .thenReturn(task -> task.complete());

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(
                PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID)
            )).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(VALIDITY);
        verify(processScenario, times(1)).hasCompleted(ARE_MCS_REQUIRED);
        verify(processScenario, times(1)).hasCompleted(MULTIPLE_CONTRIBUTIONS);
        verify(processScenario, times(1)).hasCompleted(RESPONSE_TYPE);
        verify(processScenario, times(1)).hasCompleted(UPLOAD_DRAFT);
        verify(processScenario, times(1)).hasCompleted(SENSITIVITY);
        verify(processScenario).hasFinished(END_EVENT);
        verify(processScenario, never()).waitsAtUserTask(EXEMPTION);
        verify(processScenario, never()).waitsAtUserTask(INVALID);
    }

    @Test
    public void exemption() {

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT))
            .thenReturn(task -> task.complete(withVariables(
                "DraftAcceptCase", "Y")));

        when(processScenario.waitsAtUserTask(VALIDITY))
            .thenReturn(task -> task.complete(withVariables(
                "DraftValidity", "Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(ARE_MCS_REQUIRED))
            .thenReturn(task -> task.complete(withVariables(
                "ContributionsRequired", "N", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(RESPONSE_TYPE))
            .thenReturn(task -> task.complete(withVariables(
                "ResponseType", "EXEMPTION", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(EXEMPTION))
            .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(UPLOAD_DRAFT))
            .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(
                PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID)
            )).execute();


        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(VALIDITY);
        verify(processScenario, times(1)).hasCompleted(ARE_MCS_REQUIRED);
        verify(processScenario, times(1)).hasCompleted(RESPONSE_TYPE);
        verify(processScenario, times(1)).hasCompleted(UPLOAD_DRAFT);
        verify(processScenario).hasFinished(END_EVENT);
        verify(processScenario, never()).waitsAtUserTask(MULTIPLE_CONTRIBUTIONS);
        verify(processScenario, never()).waitsAtUserTask(INVALID);
        verify(processScenario, never()).waitsAtUserTask(SENSITIVITY);
    }

    @Test
    public void happyPath() {

        when(processScenario.waitsAtUserTask(ACCEPT_OR_REJECT))
            .thenReturn(task -> task.complete(withVariables(
                "DraftAcceptCase", "Y")));

        when(processScenario.waitsAtUserTask(VALIDITY))
            .thenReturn(task -> task.complete(withVariables(
                "DraftValidity", "Y", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(ARE_MCS_REQUIRED))
            .thenReturn(task -> task.complete(withVariables(
                "ContributionsRequired", "N", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(RESPONSE_TYPE))
            .thenReturn(task -> task.complete(withVariables(
                "ResponseType", "FULL_DISCLOSURE", "DIRECTION", "FORWARD")));

        when(processScenario.waitsAtUserTask(UPLOAD_DRAFT))
            .thenReturn(task -> task.complete());

        when(processScenario.waitsAtUserTask(SENSITIVITY))
            .thenReturn(task -> task.complete());

        Scenario.run(processScenario).startBy(
            () -> rule.getRuntimeService().startProcessInstanceByKey(
                PROCESS_KEY, STAGE_UUID,
                Map.of("CaseUUID", CASE_UUID)
            )).execute();

        verify(processScenario, times(1)).hasCompleted(ACCEPT_OR_REJECT);
        verify(processScenario, times(1)).hasCompleted(VALIDITY);
        verify(processScenario, times(1)).hasCompleted(ARE_MCS_REQUIRED);
        verify(processScenario, times(1)).hasCompleted(RESPONSE_TYPE);
        verify(processScenario, times(1)).hasCompleted(UPLOAD_DRAFT);
        verify(processScenario, times(1)).hasCompleted(SENSITIVITY);
        verify(processScenario).hasFinished(END_EVENT);
        verify(processScenario, never()).waitsAtUserTask(MULTIPLE_CONTRIBUTIONS);
        verify(processScenario, never()).waitsAtUserTask(EXEMPTION);
        verify(processScenario, never()).waitsAtUserTask(INVALID);
    }
}
