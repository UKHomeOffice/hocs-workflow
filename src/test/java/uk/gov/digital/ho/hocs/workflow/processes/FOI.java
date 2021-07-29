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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/STAGE.bpmn",
        "processes/FOI.bpmn",
        "processes/FOI_CASE_CREATION.bpmn",
        "processes/FOI_DATA_INPUT.bpmn",
        "processes/FOI_ALLOCATION.bpmn",
        "processes/FOI_ACCEPTANCE.bpmn",
        "processes/FOI_DRAFT.bpmn",
        "processes/FOI_QA.bpmn",
        "processes/FOI_DISPATCH.bpmn",
        "processes/FOI_SOFT_CLOSE.bpmn"
})
public class FOI {

    public static final String CASE_CREATION_ACTIVITY = "CASE_CREATION";
    public static final String DATA_INPUT_ACTIVITY = "Activity_0jtkbij";
    public static final String ALLOCATION_ACTIVITY = "Activity_16l1q7b";
    public static final String COMPLETE_CASE_ACTIVITY = "COMPLETE_CASE";
    public static final String ACCEPTANCE_ACTIVITY = "ACCEPTANCE";
    public static final String FOI_DRAFT = "FOI_DRAFT";
    public static final String FOI_QA = "FOI_QA";
    public static final String FOI_PRESS_OFFICE_APPROVAL = "FOI_PRESS_OFFICE_APPROVAL";
    public static final String FOI_DISPATCH = "FOI_DISPATCH";
    public static final String FOI_SOFT_CLOSE = "FOI_SOFT_CLOSE";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario FOIProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void happyPath() {

        whenAtCallActivity("FOI_CASE_CREATION")
                .deploy(rule);

        whenAtCallActivity("FOI_DATA_INPUT")
                .deploy(rule);

        whenAtCallActivity("FOI_ALLOCATION")
            .deploy(rule);

        whenAtCallActivity("FOI_ACCEPTANCE")
                .alwaysReturn("AcceptCase", "Y")
                .deploy(rule);

        whenAtCallActivity(FOI_DRAFT)
                .alwaysReturn("DraftAcceptCase", "Y", "QaOffline", "QaOffline-N")
                .deploy(rule);

        whenAtCallActivity(FOI_QA)
                .alwaysReturn("G6orG7AcceptCase", "G6orG7AcceptCase-Y",
                        "G6orG7AcceptSensitivityLevel", "G6orG7AcceptSensitivityLevel-Y")
                .deploy(rule);

        whenAtCallActivity(FOI_PRESS_OFFICE_APPROVAL)
                .deploy(rule);

        whenAtCallActivity(FOI_DISPATCH)
                .alwaysReturn("ShouldDispatch", "ShouldDispatch-Y")

                .deploy(rule);

        whenAtCallActivity(FOI_SOFT_CLOSE)
                .thenReturn("blank", "blank")
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(2))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(DATA_INPUT_ACTIVITY);

        verify(FOIProcess, times(2))
            .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(FOI_DRAFT);

        verify(FOIProcess, times(2))
                .hasCompleted(FOI_QA);

        verify(FOIProcess, times(2))
                .hasCompleted(FOI_PRESS_OFFICE_APPROVAL);

        verify(FOIProcess, times(2))
                .hasCompleted(FOI_DISPATCH);

        verify(FOIProcess, times(2))
                .hasCompleted(FOI_SOFT_CLOSE);

        verify(bpmnService).completeCase(any());

    }

    @Test
    public void happyPath_caseQAdOffline() {

        whenAtCallActivity("FOI_CASE_CREATION")
                .deploy(rule);

        whenAtCallActivity("FOI_DATA_INPUT")
                .deploy(rule);

        whenAtCallActivity("FOI_ALLOCATION")
                .deploy(rule);

        whenAtCallActivity("FOI_ACCEPTANCE")
                .thenReturn("AcceptCase", "Y")
                .deploy(rule);

        whenAtCallActivity(FOI_DRAFT)
                .thenReturn("DraftAcceptCase", "Y", "QaOffline", "QaOffline-Y")
                .deploy(rule);

        whenAtCallActivity(FOI_DISPATCH)
                .alwaysReturn("ShouldDispatch", "ShouldDispatch-Y")
                .deploy(rule);

        whenAtCallActivity(FOI_SOFT_CLOSE)
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(DATA_INPUT_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_DRAFT);

        verify(FOIProcess, never()).waitsAtUserTask(FOI_QA);

        verify(FOIProcess, never()).waitsAtUserTask(FOI_PRESS_OFFICE_APPROVAL);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_DISPATCH);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_SOFT_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE_ACTIVITY);


        verify(bpmnService).completeCase(any());

    }

    @Test
    public void caseRejectedAtAllocation() {

        whenAtCallActivity("FOI_CASE_CREATION")
                .deploy(rule);

        whenAtCallActivity("FOI_DATA_INPUT")
                .deploy(rule);

        whenAtCallActivity("FOI_ALLOCATION")
                .deploy(rule);

        whenAtCallActivity("FOI_ACCEPTANCE")
                .thenReturn("AcceptCase", "N")
                .thenReturn("AcceptCase", "Y")
                .deploy(rule);

        whenAtCallActivity(FOI_DRAFT)
                .thenReturn("DraftAcceptCase", "Y", "QaOffline", "QaOffline-N")
                .deploy(rule);

        whenAtCallActivity(FOI_QA)
                .thenReturn("G6orG7AcceptCase", "G6orG7AcceptCase-Y",
                        "G6orG7AcceptSensitivityLevel", "G6orG7AcceptSensitivityLevel-Y")
                .deploy(rule);

        whenAtCallActivity(FOI_PRESS_OFFICE_APPROVAL)
                .deploy(rule);

        whenAtCallActivity(FOI_DISPATCH)
                .alwaysReturn("ShouldDispatch", "ShouldDispatch-Y")
                .deploy(rule);

        whenAtCallActivity(FOI_SOFT_CLOSE)
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(DATA_INPUT_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_DRAFT);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_QA);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_PRESS_OFFICE_APPROVAL);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_DISPATCH);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_SOFT_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE_ACTIVITY);

        verify(bpmnService).completeCase(any());
    }

    @Test
    public void caseRejectedByDraftTeam() {

        whenAtCallActivity("FOI_CASE_CREATION")
                .deploy(rule);

        whenAtCallActivity("FOI_DATA_INPUT")
                .deploy(rule);

        whenAtCallActivity("FOI_ALLOCATION")
                .deploy(rule);

        whenAtCallActivity("FOI_ACCEPTANCE")
                .thenReturn("AcceptCase", "Y")
                .thenReturn("AcceptCase", "Y")
                .deploy(rule);

        whenAtCallActivity("FOI_DRAFT")
                .thenReturn("DraftAcceptCase", "N")
                .thenReturn("DraftAcceptCase", "Y", "QaOffline", "QaOffline-N")
                .deploy(rule);

        whenAtCallActivity(FOI_QA)
                .thenReturn("G6orG7AcceptCase", "G6orG7AcceptCase-Y",
                        "G6orG7AcceptSensitivityLevel", "G6orG7AcceptSensitivityLevel-Y")
                .deploy(rule);

        whenAtCallActivity(FOI_PRESS_OFFICE_APPROVAL)
                .deploy(rule);

        whenAtCallActivity(FOI_DISPATCH)
                .alwaysReturn("ShouldDispatch", "ShouldDispatch-Y")
                .deploy(rule);

        whenAtCallActivity(FOI_SOFT_CLOSE)
                .thenReturn("ForceClose", "true")
                .deploy(rule);

        Scenario.run(FOIProcess)
                .startByKey("FOI")
                .execute();

        verify(FOIProcess, times(1))
                .hasCompleted("FOI_START");

        verify(FOIProcess, times(1))
                .hasCompleted(CASE_CREATION_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(DATA_INPUT_ACTIVITY);

        verify(FOIProcess, times(1))
                .hasCompleted(ALLOCATION_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(ACCEPTANCE_ACTIVITY);

        verify(FOIProcess, times(2))
                .hasCompleted(FOI_DRAFT);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_QA);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_PRESS_OFFICE_APPROVAL);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_DISPATCH);

        verify(FOIProcess, times(1))
                .hasCompleted(FOI_SOFT_CLOSE);

        verify(FOIProcess, times(1))
                .hasCompleted(COMPLETE_CASE_ACTIVITY);

        verify(bpmnService).completeCase(any());
    }
}
