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
import uk.gov.digital.ho.hocs.workflow.bpmn.TaggingService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.digital.ho.hocs.workflow.util.CallActivityMockWrapper.whenAtCallActivity;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = { "processes/PSU/PSU_COMP_COMPLAINT.bpmn" })
public class PSU_COMP_COMPLAINT {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().assertClassCoverageAtLeast(1).build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Mock
    BpmnService bpmnService;

    @Mock
    TaggingService taggingService;

    @Mock
    private ProcessScenario processScenario;

    @Before
    public void setup() {
        Mocks.register("bpmnService", bpmnService);
        Mocks.register("taggingService", taggingService);
    }

    @Test
    public void testHappyPath() {
        whenAtCallActivity("PSU")
                .thenReturn("", "")
                .deploy(rule);

        Scenario.run(processScenario)
                .startByKey("PSU_COMP_COMPLAINT", Map.of(
                        "STAGE_REGISTRATION", "PSU_REGISTRATION",
                        "STAGE_TRIAGE", "PSU_TRIAGE",
                        "STAGE_OUTCOME", "PSU_OUTCOME"
                ))
                .execute();

        verify(processScenario).hasCompleted("StartEvent_Complaint");
        verify(processScenario).hasCompleted("CallActivity_PSU");
        verify(processScenario).hasCompleted("EndEvent_Complaint");
    }

    @Test
    public void testReturnCase() {
        whenAtCallActivity("PSU")
            .thenReturn("ReturnCase", Boolean.TRUE.toString())
            .deploy(rule);

        Scenario.run(processScenario)
            .startByKey("PSU_COMP_COMPLAINT", Map.of(
                "STAGE_REGISTRATION", "PSU_REGISTRATION",
                "STAGE_TRIAGE", "PSU_TRIAGE",
                "STAGE_OUTCOME", "PSU_OUTCOME"))
            .execute();

        verify(processScenario).hasCompleted("StartEvent_Complaint");
        verify(processScenario).hasCompleted("CallActivity_PSU");
        verify(processScenario).hasCompleted("Service_ResetComplaintCategories");
        verify(bpmnService).blankCaseValues(any(), any(), eq("CompType"), eq("CatAssault"), eq("CatFraud"),
            eq("CatOtherUnprof"), eq("CatRacism"), eq("CatRude"), eq("CatSexAssault"),
            eq("CatTheft"), eq("CatUnfair"));
        verify(processScenario).hasCompleted("Service_UpdateUKVIDeadline");
        verify(bpmnService).updateDeadlineDays(any(), any(), eq("20"));
        verify(processScenario).hasCompleted("EndEvent_Complaint");
    }
}
