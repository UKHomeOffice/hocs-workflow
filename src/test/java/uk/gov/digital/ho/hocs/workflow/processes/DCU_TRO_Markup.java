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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/DCU_TRO_MARKUP.bpmn")
public class DCU_TRO_Markup extends DCU_MIN_DTEN_Markup_Common {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            //.assertClassCoverageAtLeast(0.55)
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
        setupMarkupFor("PR", dcuMinSignOffProcess);

        when(dcuMinSignOffProcess.waitsAtUserTask("VALIDATE_DCU_ANSWERING"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD",
                        "POTeamUUID", "",
                        "OverrideDraftingTeamUUID", "",
                        "OverridePOTeamUUID", ""
                )));

        Scenario.run(dcuMinSignOffProcess)
                .startByKey("DCU_TRO_MARKUP")
                .execute();

        verify(bpmnService).updatePrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC), eq("DCU_TRO_INITIAL_DRAFT"), eq("DefaultPolicyTeamUUID"), eq("DefaultPolicyTeamName"));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC), eq("DCU_TRO_INITIAL_DRAFT"), eq("DraftingTeamUUID"), eq("DraftingTeamName"));

        verify(bpmnService).updateTeamSelection(any(), any(), eq(""), eq(""));
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenPRAndWhenOverride() {
        setupMarkupFor("PR", dcuMinSignOffProcess);

        when(dcuMinSignOffProcess.waitsAtUserTask("VALIDATE_DCU_ANSWERING"))
                .thenReturn(task -> task.complete(withVariables("valid", true, "DIRECTION", "FORWARD",
                        "POTeamUUID", "",
                        "OverrideDraftingTeamUUID", DCU_DRAFT_TEAM_UUID,
                        "OverridePOTeamUUID", ""
                )));

        Scenario.run(dcuMinSignOffProcess)
                .startByKey("DCU_TRO_MARKUP")
                .execute();

        verify(bpmnService).updatePrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC), eq("DCU_TRO_INITIAL_DRAFT"), eq("DefaultPolicyTeamUUID"), eq("DefaultPolicyTeamName"));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC), eq("DCU_TRO_INITIAL_DRAFT"), eq("DraftingTeamUUID"), eq("DraftingTeamName"));

        verify(bpmnService).updateTeamSelection(any(), any(), eq(DCU_DRAFT_TEAM_UUID), eq(""));
    }

    @Test
    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenNotOverride() {
        setupMarkupFor("FAQ", dcuMinSignOffProcess);

        Scenario.run(dcuMinSignOffProcess)
                .startByKey("DCU_TRO_MARKUP")
                .execute();

        verify(bpmnService).updatePrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC), eq("DCU_TRO_INITIAL_DRAFT"), eq("DefaultPolicyTeamUUID"), eq("DefaultPolicyTeamName"));

        verify(bpmnService).setDraftingTeam(any(), any(), eq(DCU_DRAFT_TEAM_UUID));

    }

}
