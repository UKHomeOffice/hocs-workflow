package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/DCU_MIN_MARKUP.bpmn")
public class DCU_MIN_Markup extends DCU_MIN_DTEN_Markup_Common {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.55)
            .build();

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();
    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario dcuMinSignOffProcess;

    @Before
    public void setUp() {
        Mocks.register("bpmnService", bpmnService);
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenPRAndWhenNotOverride() {
        shouldSaveDraftAndPolicyTeamWhenPRAndWhenNotOverride(dcuMinSignOffProcess, "DCU_MIN_MARKUP", bpmnService, "DCU_MIN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenPRAndWhenOverride() {
        shouldSaveDraftAndPolicyTeamWhenPRAndWhenOverride(dcuMinSignOffProcess, "DCU_MIN_MARKUP", bpmnService, "DCU_MIN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenNotOverride() {
        shouldSaveDraftAndPolicyTeamWhenFAQAndWhenNotOverride(dcuMinSignOffProcess, "DCU_MIN_MARKUP", bpmnService, "DCU_MIN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenOverride() {
        shouldSaveDraftAndPolicyTeamWhenFAQAndWhenOverride(dcuMinSignOffProcess, "DCU_MIN_MARKUP", bpmnService, "DCU_MIN");
    }

}
