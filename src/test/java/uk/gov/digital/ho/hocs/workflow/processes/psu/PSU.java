package uk.gov.digital.ho.hocs.workflow.processes.psu;

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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/PSU/PSU.bpmn",
        "processes/STAGE.bpmn"})
public class PSU {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    BpmnService bpmnService;

    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void testHappyPath() {
        whenAtCallActivity("PSU_REGISTRATION")
                .thenReturn("", "")
                .deploy(rule);

        whenAtCallActivity("PSU_TRIAGE")
                .thenReturn("", "")
                .deploy(rule);

        whenAtCallActivity("PSU_OUTCOME")
                .thenReturn("", "")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("PSU", Map.of(
                        "STAGE_REGISTRATION", "PSU_REGISTRATION",
                        "STAGE_TRIAGE", "PSU_TRIAGE",
                        "STAGE_OUTCOME", "PSU_OUTCOME"
                ))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_PSU");
        verify(processScenario).hasCompleted("CallActivity_PSU_REGISTRATION");
        verify(processScenario).hasCompleted("Service_UpdatePsuDeadline");
        verify(bpmnService).updateDeadlineDays(any(), any(), eq("60"));
        verify(processScenario).hasCompleted("CallActivity_PSU_TRIAGE");
        verify(processScenario).hasCompleted("CallActivity_PSU_OUTCOME");
        verify(processScenario).hasCompleted("EndEvent_PSU");
    }

    @Test
    public void testCompleteCase() {
        whenAtCallActivity("PSU_REGISTRATION")
            .thenReturn("", "")
            .deploy(rule);

        whenAtCallActivity("PSU_TRIAGE")
            .thenReturn("PsuTriageOutcome", "CloseCase")
            .deploy(rule);

        Scenario.run(processScenario)
            .startByKey("PSU", Map.of(
                "STAGE_REGISTRATION", "PSU_REGISTRATION",
                "STAGE_TRIAGE", "PSU_TRIAGE",
                "STAGE_OUTCOME", "PSU_OUTCOME"))
            .execute();

        verify(processScenario).hasCompleted("StartEvent_PSU");
        verify(processScenario).hasCompleted("CallActivity_PSU_REGISTRATION");
        verify(processScenario).hasCompleted("Service_UpdatePsuDeadline");
        verify(bpmnService).updateDeadlineDays(any(), any(), eq("60"));
        verify(processScenario).hasCompleted("CallActivity_PSU_TRIAGE");
        verify(processScenario, times(0)).hasCompleted("CallActivity_PSU_OUTCOME");

        verify(processScenario).hasCompleted("EndEvent_PSU");
    }

}
