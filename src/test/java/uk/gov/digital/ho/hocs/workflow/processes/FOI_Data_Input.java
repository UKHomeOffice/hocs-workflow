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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/FOI_DATA_INPUT.bpmn"})
public class FOI_Data_Input {

    public static final String ALLOCATE_TO_CASE_CREATOR = "ALLOCATE_TO_CASE_CREATOR";
    public static final String SAVE_PRIMARY_CORRESPONDENT = "ServiceTask_097z7cz";
    public static final String UPDATE_DEADLINES = "ServiceTask_00xpp4j";
    public static final String SET_PRIMARY_CORRESPONDENT = "ServiceTask_1qqx9t6";
    public static final String VALIDATE_CORRESPONDENCE_DETAILS = "UserTask_0ni11p2";
    public static final String VALIDATE_SET_PRIMARY_CORRESPONDENT = "UserTask_0zpiaus";

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
    private ProcessScenario FOIDataInputProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void enterCorrespondenceDetails() {

        when(FOIDataInputProcess.waitsAtUserTask(VALIDATE_CORRESPONDENCE_DETAILS))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true)));

        when(FOIDataInputProcess.waitsAtUserTask(VALIDATE_SET_PRIMARY_CORRESPONDENT))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", false)))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "FORWARD",
                        "valid", true)))
                .thenReturn(task -> task.complete(withVariables(
                        "DIRECTION", "BACKWARDS",
                        "valid", false)));

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DATA_INPUT")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ALLOCATE_TO_CASE_CREATOR);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(UPDATE_DEADLINES);

        verify(FOIDataInputProcess, times(2))
                .hasCompleted(SET_PRIMARY_CORRESPONDENT);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(SAVE_PRIMARY_CORRESPONDENT);

    }

}
