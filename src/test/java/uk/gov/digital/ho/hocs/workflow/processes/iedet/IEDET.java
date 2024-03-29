package uk.gov.digital.ho.hocs.workflow.processes.iedet;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/IEDET/IEDET.bpmn",
        "processes/STAGE.bpmn",
        "processes/STAGE_WITH_USER.bpmn"})
public class IEDET {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    private BpmnService bpmnService;

    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testHappyPath(){
        whenAtCallActivity("IEDET_REGISTRATION")
                .thenReturn("", "")
                .deploy(rule);

        whenAtCallActivity("IEDET_TRIAGE")
                .thenReturn("", "")
                .deploy(rule);

        whenAtCallActivity("IEDET_DRAFT")
                .thenReturn("", "")
                .deploy(rule);

        whenAtCallActivity("IEDET_SEND")
                .thenReturn("", "")
                .deploy(rule);

        Scenario.run(processScenario).startByKey("IEDET").execute();

        verify(processScenario).hasCompleted("StartEvent_IEDET");
        verify(processScenario).hasCompleted("CallActivity_IEDET_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_IEDET_TRIAGE");
        verify(processScenario).hasCompleted("CallActivity_IEDET_DRAFT");
        verify(processScenario).hasCompleted("CallActivity_IEDET_SEND");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_IEDET");
    }

    @Test
    public void testAssignCch(){
        whenAtCallActivity("IEDET_REGISTRATION")
                .thenReturn("", "")
                .deploy(rule);

        whenAtCallActivity("IEDET_TRIAGE")
                .thenReturn("TriageAssign", "CCH")
                .deploy(rule);

        Scenario.run(processScenario).startByKey("IEDET").execute();

        verify(processScenario).hasCompleted("StartEvent_IEDET");
        verify(processScenario).hasCompleted("CallActivity_IEDET_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_IEDET_TRIAGE");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_IEDET");
    }

    @Test
    public void testSeriousMisconduct(){
        whenAtCallActivity("IEDET_REGISTRATION")
                .thenReturn("", "")
                .deploy(rule);

        whenAtCallActivity("IEDET_TRIAGE")
                .thenReturn("CompType", "SeriousMisconduct")
                .deploy(rule);

        whenAtCallActivity("PSU_IEDET_COMPLAINT")
            .alwaysReturn("ReturnCase", "ReturnCase-false")
            .deploy(rule);

        Scenario.run(processScenario).startByKey("IEDET").execute();

        verify(processScenario).hasCompleted("StartEvent_IEDET");
        verify(processScenario).hasCompleted("CallActivity_IEDET_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_IEDET_TRIAGE");
        verify(processScenario).hasCompleted("CallActivity_PSU_COMPLAINT");
        verify(processScenario).hasCompleted("ServiceTask_CompleteCase");
        verify(processScenario).hasCompleted("EndEvent_IEDET");
    }

    @Test
    public void testSeriousMisconductSendtoIEDetention(){
        whenAtCallActivity("IEDET_REGISTRATION")
            .thenReturn("", "")
            .deploy(rule);

        whenAtCallActivity("IEDET_TRIAGE")
            .thenReturn("CompType", "SeriousMisconduct")
            .deploy(rule);

        whenAtCallActivity("PSU_IEDET_COMPLAINT")
            .alwaysReturn("ReturnCase", "ReturnCase-true")
            .deploy(rule);

        Scenario.run(processScenario).startByKey("IEDET").execute();

        verify(processScenario).hasCompleted("StartEvent_IEDET");
        verify(processScenario).hasCompleted("CallActivity_IEDET_REGISTRATION");
        verify(processScenario).hasCompleted("CallActivity_IEDET_TRIAGE");
        verify(processScenario).hasCompleted("CallActivity_PSU_COMPLAINT");
        verify(processScenario).hasCompleted("EndEvent_IEDET");
    }

    @Test
    public void testReturnToTriageFromSend(){
        whenAtCallActivity("IEDET_REGISTRATION")
            .alwaysReturn("", "")
            .deploy(rule);

        whenAtCallActivity("IEDET_TRIAGE")
            .alwaysReturn("", "")
            .deploy(rule);

        whenAtCallActivity("IEDET_DRAFT")
            .alwaysReturn("", "")
            .deploy(rule);

        whenAtCallActivity("IEDET_SEND")
            .thenReturn("SendAction", "Triage")
            .thenReturn("SendAction", "Close")
            .deploy(rule);

        Scenario.run(processScenario).startByKey("IEDET").execute();

        verify(processScenario, times(1)).hasCompleted("StartEvent_IEDET");
        verify(processScenario, times(1)).hasCompleted("CallActivity_IEDET_REGISTRATION");
        verify(processScenario, times(2)).hasCompleted("CallActivity_IEDET_TRIAGE");
        verify(processScenario, times(2)).hasCompleted("CallActivity_IEDET_DRAFT");
        verify(processScenario, times(2)).hasCompleted("CallActivity_IEDET_SEND");
        verify(processScenario, times(1)).hasCompleted("EndEvent_IEDET");
    }
}
