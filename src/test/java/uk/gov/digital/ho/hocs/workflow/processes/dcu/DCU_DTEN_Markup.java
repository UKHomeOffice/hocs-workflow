package uk.gov.digital.ho.hocs.workflow.processes.dcu;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/DCU/DCU_DTEN_MARKUP.bpmn")
public class DCU_DTEN_Markup extends DCU_MIN_DTEN_Markup_Common {

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
        shouldSaveDraftAndPolicyTeamWhenPRAndWhenNotOverride(dcuMinSignOffProcess, "DCU_DTEN_MARKUP", bpmnService, "DCU_DTEN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenPRAndWhenOverride() {
        shouldSaveDraftAndPolicyTeamWhenPRAndWhenOverride(dcuMinSignOffProcess, "DCU_DTEN_MARKUP", bpmnService, "DCU_DTEN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenNotOverride() {
        shouldSaveDraftAndPolicyTeamWhenFAQAndWhenNotOverride(dcuMinSignOffProcess, "DCU_DTEN_MARKUP", bpmnService, "DCU_DTEN");
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenOverride() {
        shouldSaveDraftAndPolicyTeamWhenFAQAndWhenOverride(dcuMinSignOffProcess, "DCU_DTEN_MARKUP", bpmnService, "DCU_DTEN");
    }

}
