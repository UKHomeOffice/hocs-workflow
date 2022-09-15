package uk.gov.digital.ho.hocs.workflow.processes.dcu;

import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DCU_MIN_DTEN_Markup_Common {

    public static final String POINTY_THINGS_TOPIC = "9b113586-6a69-4b44-802f-b27acce94753";

    public static final String POINTY_THINGS_TOPIC_TEAM_UUID = "5225a3bf-8f4d-4126-a787-eb4fa9901b57";

    public static final String POINTY_THINGS_TOPIC_TEAM_NAME = "Serious Violence Unit";

    public static final String ANOTHER_TOPIC_TEAM_UUID = "10fdfdfc-ed36-487c-8bb2-fcd4c6c8fe49";

    public static final String DCU_DRAFT_TEAM_UUID = "f5fdfdfc-ed36-487c-8bb2-fcd4c6c8fe49";

    public void shouldSaveDraftAndPolicyTeamWhenPRAndWhenNotOverride(ProcessScenario dcuMinSignOffProcess,
                                                                     String key,
                                                                     BpmnService bpmnService,
                                                                     String stagePrefix) {

        setupMarkupFor("PR", dcuMinSignOffProcess);

        when(dcuMinSignOffProcess.waitsAtUserTask("VALIDATE_DCU_ANSWERING_PR")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "POTeamUUID", "", "OverrideDraftingTeamUUID", "",
                "OverridePOTeamUUID", "", "OverridePOTeamUnitHistoricName", "")));

        Scenario.run(dcuMinSignOffProcess).startByKey(key).execute();

        verifyCommonPRFlow(bpmnService, stagePrefix);

        verify(bpmnService).updateTeamSelection(any(), any(), eq(""), eq(""));

    }

    public void shouldSaveDraftAndPolicyTeamWhenPRAndWhenOverride(ProcessScenario dcuMinSignOffProcess,
                                                                  String key,
                                                                  BpmnService bpmnService,
                                                                  String stagePrefix) {

        setupMarkupFor("PR", dcuMinSignOffProcess);

        when(dcuMinSignOffProcess.waitsAtUserTask("VALIDATE_DCU_ANSWERING_PR")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "POTeamUUID", "", "OverrideDraftingTeamUUID",
                DCU_DRAFT_TEAM_UUID, "OverridePOTeamUUID", "", "POTeamNameUnitHistoricName", "")));

        Scenario.run(dcuMinSignOffProcess).startByKey(key).execute();

        verifyCommonPRFlow(bpmnService, stagePrefix);

        verify(bpmnService).updateTeamSelection(any(), any(), eq(DCU_DRAFT_TEAM_UUID), eq(""));
    }

    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenNotOverride(ProcessScenario dcuMinSignOffProcess,
                                                                      String key,
                                                                      BpmnService bpmnService,
                                                                      String stagePrefix) {

        setupMarkupFor("FAQ", dcuMinSignOffProcess);

        when(dcuMinSignOffProcess.waitsAtUserTask("VALIDATE_DCU_ANSWERING_FAQ")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "POTeamUUID", "", "OverridePOTeamUUID", "",
                "OverridePOTeamUnitHistoricName", "")));

        Scenario.run(dcuMinSignOffProcess).startByKey(key).execute();

        verifyCommonFAQFlow(bpmnService, stagePrefix);

        verify(bpmnService).updateTeamSelection(any(), any(), eq(""), eq(""));
    }

    public void shouldSaveDraftAndPolicyTeamWhenFAQAndWhenOverride(ProcessScenario dcuMinSignOffProcess,
                                                                   String key,
                                                                   BpmnService bpmnService,
                                                                   String stagePrefix) {

        setupMarkupFor("FAQ", dcuMinSignOffProcess);

        when(dcuMinSignOffProcess.waitsAtUserTask("VALIDATE_DCU_ANSWERING_FAQ")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "POTeamUUID", "", "OverridePOTeamUUID",
                ANOTHER_TOPIC_TEAM_UUID, "OverridePOTeamUnitHistoricName", "")));

        Scenario.run(dcuMinSignOffProcess).startByKey(key).execute();

        verifyCommonFAQFlow(bpmnService, stagePrefix);

        verify(bpmnService).updateTeamSelection(any(), any(), eq(""), eq(ANOTHER_TOPIC_TEAM_UUID));
    }

    void setupMarkupFor(String PR, ProcessScenario dcuMinSignOffProcess) {
        when(dcuMinSignOffProcess.waitsAtUserTask("VALIDATE_DCU_MARKUP_DECISION")).thenReturn(
            task -> task.complete(withVariables("valid", true, "MarkupDecision", PR)));
        when(dcuMinSignOffProcess.waitsAtUserTask("VALIDATE_TOPICS")).thenReturn(task -> task.complete(
            withVariables("valid", true, "DIRECTION", "FORWARD", "Topics", POINTY_THINGS_TOPIC, "DefaultPolicyTeamUUID",
                POINTY_THINGS_TOPIC_TEAM_UUID, "DefaultPolicyTeamName", POINTY_THINGS_TOPIC_TEAM_NAME)));
    }

    void verifyCommonPRFlow(BpmnService bpmnService, String stagePrefix) {
        verify(bpmnService).updatePrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC),
            eq(stagePrefix + "_INITIAL_DRAFT"), eq("DefaultPolicyTeamUUID"), eq("DefaultPolicyTeamName"),
            eq("DefaultPolicyTeamUnitHistoricName"));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC),
            eq(stagePrefix + "_INITIAL_DRAFT"), eq("DraftingTeamUUID"), eq("DraftingTeamName"),
            eq("DraftingTeamUnitHistoricName"));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC),
            eq(stagePrefix + "_PRIVATE_OFFICE"), eq("POTeamUUID"), eq("POTeamName"), eq("POTeamUnitHistoricName"));
    }

    void verifyCommonFAQFlow(BpmnService bpmnService, String stagePrefix) {
        verify(bpmnService).updatePrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC),
            eq(stagePrefix + "_INITIAL_DRAFT"), eq("DefaultPolicyTeamUUID"), eq("DefaultPolicyTeamName"),
            eq("DefaultPolicyTeamUnitHistoricName"));

        verify(bpmnService).setDraftingTeam(any(), any(), eq(DCU_DRAFT_TEAM_UUID));

        verify(bpmnService).updateTeamsForPrimaryTopic(any(), any(), eq(POINTY_THINGS_TOPIC),
            eq(stagePrefix + "_PRIVATE_OFFICE"), eq("POTeamUUID"), eq("POTeamName"), eq("POTeamUnitHistoricName"));
    }

}
