package uk.gov.digital.ho.hocs.workflow.processes.mpam;

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

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM/MPAM_CREATION.bpmn")
public class MPAMCreation {

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
    private ProcessScenario mpamCreationProcess;

    @Before
    public void defaultScenario() {

        Mocks.register("bpmnService", bpmnService);

        when(mpamCreationProcess.waitsAtUserTask("UserTask_145n012"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "BusArea", "")));
        when(mpamCreationProcess.waitsAtUserTask("UserTask_0iez602"))
                .thenReturn(task -> task.complete(withVariables("DIRECTION", "FORWARD")));
    }

    @Test
    public void happyPath() {

        when(bpmnService.caseHasMember(any())).thenReturn(true);

        Scenario.run(mpamCreationProcess)
                .startByKey("MPAM_CREATION")
                .execute();

        verify(mpamCreationProcess)
                .hasCompleted("ServiceTask_1wekfef");
        verify(mpamCreationProcess)
                .hasCompleted("ServiceTask_0wdqurs");
        verify(mpamCreationProcess)
                .hasFinished("EndEvent_0cpzydi");

        verify(bpmnService).updatePrimaryCorrespondent(any(), any(), any());

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), any(), any(), any(), any(), any());

    }

    @Test
    public void hasNoCaseMember() {

        when(bpmnService.caseHasMember(any()))
                .thenReturn(false)
                .thenReturn(true);

        when(mpamCreationProcess.waitsAtUserTask("Activity_0oz6tve"))
                .thenReturn(task -> task.complete(withVariables("valid", true)));

        Scenario.run(mpamCreationProcess)
                .startByKey("MPAM_CREATION")
                .execute();

        verify(mpamCreationProcess, times(2))
                .hasCompleted("ServiceTask_1wekfef");
        verify(mpamCreationProcess)
                .hasCompleted("ServiceTask_0wdqurs");
        verify(mpamCreationProcess)
                .hasCompleted("Activity_1jsutsb");
        verify(mpamCreationProcess)
                .hasCompleted("Activity_0oz6tve");
        verify(mpamCreationProcess)
                .hasFinished("EndEvent_0cpzydi");

        verify(bpmnService, times(2)).updatePrimaryCorrespondent(any(), any(), any());

        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), any(), any(), any(), any(), any());

    }

}
