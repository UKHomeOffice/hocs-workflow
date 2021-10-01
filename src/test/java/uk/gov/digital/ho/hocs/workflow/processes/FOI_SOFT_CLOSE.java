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
        "processes/FOI_SOFT_CLOSE.bpmn"})
public class FOI_SOFT_CLOSE {

    public static final String SOFT_CLOSE = "SOFT_CLOSE";
    public static final String DEALLOCATE_TEAM = "DEALLOCATE_TEAM";
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
    public void happyPath() {
        when(FOIDataInputProcess.waitsAtUserTask(SOFT_CLOSE))
                .thenReturn(task -> task.complete());

        Scenario.run(FOIDataInputProcess)
                .startByKey("FOI_SOFT_CLOSE")
                .execute();

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(SOFT_CLOSE);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(DEALLOCATE_TEAM);

        verify(FOIDataInputProcess, times(1))
                .hasCompleted(END_EVENT);

    }
}
