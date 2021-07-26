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
        "processes/FOI_DISPATCH.bpmn"})
public class FOI_DISPATCH {

    public static final String ALLOCATE_TO_CASE_CREATOR = "ALLOCATE_TO_CASE_CREATOR";
    public static final String DISPATCH_CONFIRMATION = "DISPATCH_CONFIRMATION";
    public static final String DEALLOCATE_TEAM = "DEALLOCATE_TEAM";
    public static final String SET_DISPATCH_DATE = "SET_DISPATCH_DATE";
    public static final String CLEAR_REJECTED = "CLEAR_REJECTED";
    public static final String SET_TO_REJECTED_BY_DISPATCH = "SET_TO_REJECTED_BY_DISPATCH";
    public static final String END_EVENT = "END_EVENT";


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
    private ProcessScenario FOIDataInputProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void doNotDispatch() {

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-N")));

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ALLOCATE_TO_CASE_CREATOR);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(0))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(SET_TO_REJECTED_BY_DISPATCH);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }

    @Test
    public void doDispatch() {

        when(FOIDataInputProcess.waitsAtUserTask(DISPATCH_CONFIRMATION))
                .thenReturn(task -> task.complete(withVariables(
                        "ShouldDispatch", "ShouldDispatch-Y")));

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_DISPATCH")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(ALLOCATE_TO_CASE_CREATOR);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DISPATCH_CONFIRMATION);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(SET_DISPATCH_DATE);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(CLEAR_REJECTED);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }
}
