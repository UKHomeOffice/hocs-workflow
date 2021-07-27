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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/FOI_CASE_CREATION.bpmn"})
public class FOI_CASE_CREATION {

    public static final String ALLOCATE_TO_CASE_CREATOR = "ALLOCATE_TO_CASE_CREATOR";
    public static final String CHECK_ANSWERS = "CHECK_ANSWERS";
    public static final String CHANGE_ANSWERS = "CHANGE_ANSWERS";
    public static final String UPDATE_DEADLINES = "UPDATE_DEADLINES";
    public static final String CHECK_VALIDITY = "CHECK_VALIDITY";
    public static final String VALID_TEMPLATES = "VALID_TEMPLATES";
    public static final String NON_VALID_TEMPLATES = "NON_VALID_TEMPLATES";
    public static final String END_EVENT = "END_EVENT";
    public static final String SAVE_PRIMARY_TOPIC_PRE_CHANGE = "Activity_1xbxkjm";
    public static final String SAVE_PRIMARY_TOPIC_POST_CHANGE = "Activity_0krulfl";



    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.575)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario FOICaseCreationProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testChangeAnswersOnceThenAcceptThenValidResponse() {

        when(FOICaseCreationProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "CHANGE_HOME_OFFICE_DATE_RECEIVED")))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD")));

        when(FOICaseCreationProcess.waitsAtUserTask(CHANGE_ANSWERS))
                .thenReturn(task -> task.complete());

        when(FOICaseCreationProcess.waitsAtUserTask(CHECK_VALIDITY))
                .thenReturn(task -> task.complete(withVariables(
                        "RequestValidity", "RequestValid-Y")));

        when(FOICaseCreationProcess.waitsAtUserTask(VALID_TEMPLATES))
                .thenReturn(task -> task.complete());

        Scenario.run(FOICaseCreationProcess)
                .startByKey("FOI_CASE_CREATION")
                .execute();

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(ALLOCATE_TO_CASE_CREATOR);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(SAVE_PRIMARY_TOPIC_PRE_CHANGE);

        verify(FOICaseCreationProcess, times(2))
                .hasCompleted(CHECK_ANSWERS);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(CHANGE_ANSWERS);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(UPDATE_DEADLINES);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(CHECK_VALIDITY);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(VALID_TEMPLATES);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(SAVE_PRIMARY_TOPIC_POST_CHANGE);

        verify(FOICaseCreationProcess).hasFinished(END_EVENT);

    }

    @Test
    public void testAnswersThenInvalidResponse() {

        when(FOICaseCreationProcess.waitsAtUserTask(CHECK_ANSWERS))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD")));

        when(FOICaseCreationProcess.waitsAtUserTask(CHECK_VALIDITY))
                .thenReturn(task -> task.complete(withVariables(
                        "RequestValidity", "RequestValid-N")));

        when(FOICaseCreationProcess.waitsAtUserTask(NON_VALID_TEMPLATES))
                .thenReturn(task -> task.complete());

        Scenario.run(FOICaseCreationProcess)
                .startByKey("FOI_CASE_CREATION")
                .execute();

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(ALLOCATE_TO_CASE_CREATOR);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(CHECK_ANSWERS);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(CHECK_VALIDITY);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(NON_VALID_TEMPLATES);

        verify(FOICaseCreationProcess, times(1))
                .hasCompleted(SAVE_PRIMARY_TOPIC_PRE_CHANGE);

        // NOT INVOKED

        verify(FOICaseCreationProcess, times(0))
                .hasCompleted(CHANGE_ANSWERS);

        verify(FOICaseCreationProcess, times(0))
                .hasCompleted(SAVE_PRIMARY_TOPIC_POST_CHANGE);

        verify(FOICaseCreationProcess, times(0))
                .hasCompleted(UPDATE_DEADLINES);

        // END NOT INVOKED

        verify(FOICaseCreationProcess).hasFinished(END_EVENT);

    }
}
