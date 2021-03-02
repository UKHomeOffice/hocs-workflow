package uk.gov.digital.ho.hocs.workflow.processes;

import org.junit.Test;

import static uk.gov.digital.ho.hocs.workflow.util.CallActivityTestUtil.callActivity;

public class DCU_MIN_DTEN_TRO_CallActivityVariables {

    public static final String DCU_MIN_BPMN = "processes/DCU_MIN.bpmn";
    public static final String DCU_DTEN_BPMN = "processes/DCU_DTEN.bpmn";
    public static final String DCU_TRO_BPMN = "processes/DCU_TRO.bpmn";
    public static final String DRAFT_OVERRIDE_EXPRESSION = "${execution.getVariable(\"OverrideDraftingTeamUUID\") == \"\" ? execution.getVariable(\"DraftingTeamUUID\") : execution.getVariable(\"OverrideDraftingTeamUUID\") }";
    public static final String PO_OVERRIDE_EXPRESSION = "${execution.getVariable(\"PrivateOfficeOverridePOTeamUUID\") == null ? (execution.getVariable(\"OverridePOTeamUUID\") == \"\" ? execution.getVariable(\"POTeamUUID\") : execution.getVariable(\"OverridePOTeamUUID\")) : execution.getVariable(\"PrivateOfficeOverridePOTeamUUID\") }";

    @Test
    public void MIN_MarkUpHasOverrideVariables() {
        callActivity("MarkUp_CallActivity", DCU_MIN_BPMN)
                .containsInputVariable("\"\"", "DraftingTeamUUID")
                .containsOutputVariable("DraftingTeamUUID", "DraftingTeamUUID")
                .containsOutputVariable("POTeamUUID", "POTeamUUID")
                .containsOutputVariable("OverrideDraftingTeamUUID", "OverrideDraftingTeamUUID")
                .containsOutputVariable("OverridePOTeamUUID", "OverridePOTeamUUID")
                .assertVariables();
    }

    @Test
    public void DTEN_MarkUpHasOverrideVariables() {
        callActivity("MarkUp_CallActivity", DCU_DTEN_BPMN)
                .containsInputVariable("\"\"", "DraftingTeamUUID")
                .containsOutputVariable("DraftingTeamUUID", "DraftingTeamUUID")
                .containsOutputVariable("POTeamUUID", "POTeamUUID")
                .containsOutputVariable("OverrideDraftingTeamUUID", "OverrideDraftingTeamUUID")
                .containsOutputVariable("OverridePOTeamUUID", "OverridePOTeamUUID")
                .assertVariables();
    }

    @Test
    public void TRO_MarkUpHasOverrideVariables() {
        callActivity("MarkUp_CallActivity", DCU_TRO_BPMN)
                .containsInputVariable("\"\"", "DraftingTeamUUID")
                .containsOutputVariable("DraftingTeamUUID", "DraftingTeamUUID")
                .containsOutputVariable("OverrideDraftingTeamUUID", "OverrideDraftingTeamUUID")
                .assertVariables();
    }

    @Test
    public void MIN_InitialDraftHasOverrideVariables() {
        callActivity("InitialDraft_CallActivity", DCU_MIN_BPMN)
                .containsInputVariable(DRAFT_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void DTEN_InitialDraftHasOverrideVariables() {
        callActivity("InitialDraft_CallActivity", DCU_DTEN_BPMN)
                .containsInputVariable(DRAFT_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void TRO_InitialDraftHasOverrideVariables() {
        callActivity("InitialDraft_CallActivity", DCU_TRO_BPMN)
                .containsInputVariable(DRAFT_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void MIN_QAResponseHasOverrideVariables() {
        callActivity("QAResponse_CallActivity", DCU_MIN_BPMN)
                .containsInputVariable(DRAFT_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void DTEN_QAResponseHasOverrideVariables() {
        callActivity("QAResponse_CallActivity", DCU_DTEN_BPMN)
                .containsInputVariable(DRAFT_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void TRO_QAResponseHasOverrideVariables() {
        callActivity("QAResponse_CallActivity", DCU_TRO_BPMN)
                .containsInputVariable(DRAFT_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void MIN_POSignOffHasOverrideVariables() {
        callActivity("POSignOff_CallActivity", DCU_MIN_BPMN)
                .containsInputVariable(PO_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void DTEN_POSignOffHasOverrideVariables() {
        callActivity("POSignOff_CallActivity", DCU_DTEN_BPMN)
                .containsInputVariable(PO_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void MIN_MinisterSignOffHasOverrideVariables() {
        callActivity("MinisterSignOff_CallActivity", DCU_MIN_BPMN)
                .containsInputVariable(PO_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }

    @Test
    public void TRO_DispatchHasOverrideVariables() {
        callActivity("Dispatch_CallActivity", DCU_TRO_BPMN)
                .containsInputVariable(DRAFT_OVERRIDE_EXPRESSION, "AllocationTeamUUID")
                .assertVariables();
    }
}
