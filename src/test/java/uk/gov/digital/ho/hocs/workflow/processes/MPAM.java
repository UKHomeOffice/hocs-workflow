package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.mockito.ProcessExpressions;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRule;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.util.CallActivityReturnVariable;
import uk.gov.digital.ho.hocs.workflow.util.ExecutionVariableSequence;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = {
        "processes/MPAM.bpmn",
        "processes/MPAM_CREATION.bpmn",
        "processes/MPAM_TRANSFER.bpmn",
        "processes/MPAM_TRIAGE.bpmn",
        "processes/MPAM_TRIAGE_ESCALATE.bpmn",
        "processes/MPAM_DRAFT.bpmn",
        "processes/MPAM_PO.bpmn",
        "processes/MPAM_QA.bpmn",
        "processes/MPAM_DRAFT_REQUESTED_CONTRIBUTION.bpmn",
        "processes/MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION.bpmn",
        "processes/MPAM_DRAFT_ESCALATE.bpmn",
        "processes/MPAM_CAMPAIGN.bpmn",
        "processes/STAGE.bpmn",
        "processes/STAGE_WITH_USER.bpmn"})
public class MPAM {

    public static final String DRAFT_REQUEST_CONTRIBUTION = "CallActivity_1068j5g";
    public static final String DRAFT_REQUEST_CONTRIBUTION_RESULT = "ExclusiveGateway_1hlhu8m";
    public static final String DRAFT_ESCALATED_FROM_REQUEST_CONTRIBUTION = "CallActivity_DraftEscalated_FromRequestContribution";

    public static final String DRAFT_REQUEST_CONTRIBUTION_ESCALATED_RESULT = "Gateway_0kabhfi";
    public static final String DRAFT_ESCALATE_DRAFT_STATUS = "ExclusiveGateway_1nyjaew";
    public static final String CAMPAIGN = "CallActivity_0l0wizp";

    public static final String DRAFT_CLEAR_USER = "Activity_1l35uib";
    public static final String TRANSFER_CLEAR_USER = "Activity_1klegia";
    public static final String AWAITING_TRANSFER = "Activity_0esggvd";
    public static final String SAVE_DEADLINE_GATEWAY = "Gateway_1rtuzdz";
    public static final String TRANSFER_ACCEPTED_GATEWAY = "Gateway_0xmbo1i";
    public static final String AWAITING_DISPATCH_MINISTERIAL = "CallActivity_0g8kpaf";
    public static final String COMPLETE_CASE = "ServiceTask_0rwk9ie";

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .build();

    @Mock
    BpmnService bpmnService;
    @Mock
    private ProcessScenario mpamProcess;

    @Before
    public void defaultScenario() {

        Mocks.register("bpmnService", bpmnService);

        ProcessExpressions.registerCallActivityMock("MPAM_CREATION")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRANSFER")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionAddVariable("RefType", "Ministerial")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_QA")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_PO")
                .onExecutionAddVariable("PoStatus", "Approved-Ministerial-Dispatch")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_PO_APPROVED_MIN_DISPATCH")
                .onExecutionAddVariable("MPAMDispatchStatus", "DispatchAndClose")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DISPATCHED_FOLLOW_UP")
                .deploy(rule);
    }

    @Test
    public void happyPath() {

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(AWAITING_DISPATCH_MINISTERIAL);
        verify(mpamProcess)
                .hasCompleted(COMPLETE_CASE);

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void putInCampaign_fromContributionRequestedEscalation() {
        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("DraftStatus", "RequestContribution")),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("DraftStatus", ""),
                                        new CallActivityReturnVariable("RefType", "Ministerial")
                                )
                        )
                ))
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftRequestedContributionOutcome", "Escalate")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftEscalatedRequestedContributionOutcome", "PutOnCampaign")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_CAMPAIGN")
                .onExecutionAddVariable("CampaignOutcome", "SendToDraft")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_RESULT);
        verify(mpamProcess)
                .hasCompleted(DRAFT_ESCALATED_FROM_REQUEST_CONTRIBUTION);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_ESCALATED_RESULT);
        verify(mpamProcess)
                .hasCompleted(DRAFT_ESCALATE_DRAFT_STATUS);
        verify(mpamProcess)
                .hasCompleted(CAMPAIGN);
    }

    @Test
    public void removeDraftUser_fromContributionRequested_specifyUnallocate() {
        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("DraftStatus", "RequestContribution")),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("DraftStatus", ""),
                                        new CallActivityReturnVariable("RefType", "Ministerial")
                                )
                        )
                ))
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftRequestedContributionOutcome", "Complete")
                .onExecutionAddVariable("DraftShouldUnallocate", "Unallocate")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_RESULT);
        verify(mpamProcess)
                .hasCompleted(DRAFT_CLEAR_USER);
    }

    @Test
    public void removeDraftUser_fromContributionRequested_specifyRetain() {
        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("DraftStatus", "RequestContribution")),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("DraftStatus", ""),
                                        new CallActivityReturnVariable("RefType", "Ministerial")
                                )
                        )
                ))
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftRequestedContributionOutcome", "Complete")
                .onExecutionAddVariable("DraftShouldUnallocate", "Retail")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION);
        verify(mpamProcess)
                .hasCompleted(DRAFT_REQUEST_CONTRIBUTION_RESULT);
        verify(mpamProcess, never())
                .hasCompleted(DRAFT_CLEAR_USER);
    }

    @Test
    public void clearTransferUser_fromTriageToTransferStage() {
        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("BusArea", "TransferToOgd")),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusArea", "TransferToOther")
                                )
                        )
                ))
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRANSFER")
                .onExecutionAddVariable("TransferOutcome", "TransferAccepted")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(TRANSFER_CLEAR_USER);
        verify(mpamProcess)
                .hasCompleted(AWAITING_TRANSFER);
        verify(mpamProcess)
                .hasCompleted(SAVE_DEADLINE_GATEWAY);
        verify(mpamProcess)
                .hasCompleted(TRANSFER_ACCEPTED_GATEWAY);
        verify(mpamProcess)
                .hasCompleted(COMPLETE_CASE);

    }

    @Test
    public void clearTransferUser_fromDraftToTransferStage() {
        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("BusArea", "TransferToOgd")),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusArea", "TransferToOther")
                                )
                        )
                ))
                .deploy(rule);


        ProcessExpressions.registerCallActivityMock("MPAM_TRANSFER")
                .onExecutionAddVariable("TransferOutcome", "TransferAccepted")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted(TRANSFER_CLEAR_USER);
        verify(mpamProcess)
                .hasCompleted(AWAITING_TRANSFER);
        verify(mpamProcess)
                .hasCompleted(SAVE_DEADLINE_GATEWAY);
        verify(mpamProcess)
                .hasCompleted(TRANSFER_ACCEPTED_GATEWAY);
        verify(mpamProcess)
                .hasCompleted(COMPLETE_CASE);

    }


    @Test
    public void whenTriageChangeBusinessArea_thenBusAreaStatusIsConfirmed() {

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", "Confirm"),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                )
                        )
                ))
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_Triage");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");


    }

    @Test
    public void whenDraftChangeBusinessArea_thenBusAreaStatusIsConfirmed() {

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", "Confirm"),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", ""),
                                        new CallActivityReturnVariable("DraftStatus", ""),
                                        new CallActivityReturnVariable("RefType", "Ministerial")
                                )
                        )
                ))
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Triage");
        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_Draft");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");

    }

    @Test
    public void whenTriageEscalatedAfterRequestContributionChangeBusinessArea_thenBusAreaStatusIsConfirmed() {

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .onExecutionAddVariable("TriageOutcome", "RequestContribution")
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("TriageRequestedContributionOutcome", "Escalate")
                .deploy(rule);


        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", "Confirm"),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("TriageEscalatedRequestedContributionOutcome", "Close"),
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                )
                        )
                ))
                .deploy(rule);


        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Triage");

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_TriageEscalated_FromRequestContribution");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenTriageEscalatedContributionsRequestedAfterContributionsReceived_thenTriageEscalated() {

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .onExecutionAddVariable("TriageOutcome", "RequestContribution")
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("TriageRequestedContributionOutcome", "Escalate")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("TriageEscalatedRequestedContributionOutcome", "Complete")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_ESCALATE")
                .onExecutionAddVariable("TriageEscalateOutcome", "Close")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Triage");

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_TriageEscalated_FromRequestContribution");


        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_TriageEscalated_SendToWorkflowManager");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenTriageEscalatedAfterContributionsRequested_thenTriageEscalatedContributionsRequested() {

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .onExecutionAddVariable("TriageOutcome", "SendToWorkflowManager")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_ESCALATE")
                .onExecutionAddVariable("TriageEscalateOutcome", "RequestContribution")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("TriageEscalatedRequestedContributionOutcome", "Close")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Triage");

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_TriageEscalated_SendToWorkflowManager");

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_TriageEscalated_FromRequestContribution");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenTriageEscalatedAfterContributionsRequestedCompleteed_thenTriageEscalatedContributions() {

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .onExecutionAddVariable("TriageOutcome", "SendToWorkflowManager")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("TriageEscalatedRequestedContributionOutcome", "Complete")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_ESCALATE")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("TriageEscalateOutcome", "RequestContribution"),
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("TriageEscalateOutcome", "Close"),
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                )
                        )
                ))
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Triage");

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_TriageEscalated_SendToWorkflowManager");

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_TriageEscalated_FromRequestContribution");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenTriageEscalatedSendToWorkflowManagerChangeBusinessArea_thenBusAreaStatusIsConfirmed() {

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE")
                .onExecutionAddVariable("TriageOutcome", "SendToWorkflowManager")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_TRIAGE_ESCALATE")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", "Confirm"),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("TriageEscalateOutcome", "Close"),
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                )
                        )
                ))
                .deploy(rule);


        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Triage");

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_TriageEscalated_SendToWorkflowManager");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");

    }

    @Test
    public void whenDraftEscalatedAfterRequestContributionChangeBusinessArea_thenBusAreaStatusIsConfirmed() {

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionAddVariable("DraftStatus", "RequestContribution")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftRequestedContributionOutcome", "Escalate")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", "Confirm"),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", ""),
                                        new CallActivityReturnVariable(
                                                "DraftEscalatedRequestedContributionOutcome",
                                                "Close")
                                )
                        )
                ))
                .deploy(rule);


        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Triage");
        verify(mpamProcess)
                .hasCompleted("CallActivity_Draft");

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_DraftEscalated_FromRequestContribution");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");

    }

    @Test
    public void whenDraftEscalatedAfterDraftEscalateChangeBusinessArea_thenBusAreaStatusIsConfirmed() {

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionAddVariable("DraftStatus", "Escalate")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATE")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", "Confirm"),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", ""),
                                        new CallActivityReturnVariable("DraftStatus", "Close")
                                )
                        )
                ))
                .deploy(rule);


        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Triage");
        verify(mpamProcess)
                .hasCompleted("CallActivity_Draft");

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_DraftEscalated");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");

    }


    @Test
    public void whenDraftEscalatedContributionsRequestedAfterContributionsReceived_thenDraftEscalated() {

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionAddVariable("DraftStatus", "RequestContribution")
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftRequestedContributionOutcome", "Escalate")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftEscalatedRequestedContributionOutcome", "Complete")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATE")
                .onExecutionAddVariable("DraftStatus", "Close")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Draft");

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_DraftEscalated_FromRequestContribution");


        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_DraftEscalated_FromRequestContribution");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenDraftEscalatedAfterContributionsRequested_thenDraftEscalatedContributionsRequested() {

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionAddVariable("DraftStatus", "Escalate")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATE")
                .onExecutionAddVariable("DraftStatus", "RequestContribution")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftEscalatedRequestedContributionOutcome", "Close")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Draft");

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_DraftEscalated");

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_DraftEscalated_FromRequestContribution");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenDraftEscalatedContributionsRequestedComplete_thenDraftEscalated() {

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT")
                .onExecutionAddVariable("DraftStatus", "Escalate")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATE")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("DraftStatus", "RequestContribution"),
                                        new CallActivityReturnVariable("RefTypeStatus", "")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("BusAreaStatus", ""),
                                        new CallActivityReturnVariable("RefTypeStatus", ""),
                                        new CallActivityReturnVariable("DraftStatus", "Close")
                                )
                        )
                ))
                .deploy(rule);


        ProcessExpressions.registerCallActivityMock("MPAM_DRAFT_ESCALATED_REQUESTED_CONTRIBUTION")
                .onExecutionAddVariable("DraftEscalatedRequestedContributionOutcome", "Complete")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess)
                .hasCompleted("CallActivity_Draft");

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_DraftEscalated");

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_DraftEscalated_FromRequestContribution");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenAwaitingMinDispatchBackwards_ThenCompleteCase() {
        ProcessExpressions.registerCallActivityMock("MPAM_PO_APPROVED_MIN_DISPATCH")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("MPAMDispatchStatus", "MoveBack")
                                ),
                                // second call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("MPAMDispatchStatus", "DispatchAndClose")
                                )
                        )
                ))
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_0wfokmb");
    }

    @Test
    public void whenAwaitingLocalDispatchBackwards_ThenCompleteCase() {
        ProcessExpressions.registerCallActivityMock("MPAM_PO")
                .onExecutionAddVariable("PoStatus", "Approved-Local-Dispatch")
                .deploy(rule);

        ProcessExpressions.registerCallActivityMock("MPAM_PO_APPROVED_LOCAL_DISPATCH")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("MPAMDispatchStatus", "MoveBack")
                                ),
                                // second call
                                Collections.singletonList(
                                        new CallActivityReturnVariable("MPAMDispatchStatus", "DispatchAndClose")
                                )
                        )
                ))
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_0wfokmb");
    }

    @Test
    public void whenQaClearanceRequestCancelled_thenReturnToQa() {

        ProcessExpressions.registerCallActivityMock("MPAM_QA")
                .onExecutionAddVariable("QaStatus", "RequestSecretariatClearance")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("QaStatus", "RequestSecretariatClearance")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("QaStatus", "CarryOn")
                                )
                        )
                ))
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("MPAM_QA_CLEARANCE_REQ")
                .onExecutionAddVariable("ClearanceStatus", "Cancelled")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_QaClearanceReq");


        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_1rhesrm");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenQaClearanceRequestAccepted_thenContinueToPO() {

        ProcessExpressions.registerCallActivityMock("MPAM_QA")
                .onExecutionAddVariable("QaStatus", "RequestSecretariatClearance")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("QaStatus", "RequestSecretariatClearance")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("QaStatus", "CarryOn")
                                )
                        )
                ))
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("MPAM_QA_CLEARANCE_REQ")
                .onExecutionAddVariable("ClearanceStatus", "ApprovePO")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_QaClearanceReq");


        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_1rhesrm");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @Test
    public void whenQaClearanceRequestRejected_thenGoBackToDraft() {

        ProcessExpressions.registerCallActivityMock("MPAM_QA")
                .onExecutionAddVariable("QaStatus", "RequestSecretariatClearance")
                .onExecutionDo(new ExecutionVariableSequence(
                        Arrays.asList(
                                // first call
                                Arrays.asList(
                                        new CallActivityReturnVariable("QaStatus", "RequestSecretariatClearance")
                                ),
                                // second call
                                Arrays.asList(
                                        new CallActivityReturnVariable("QaStatus", "CarryOn")
                                )
                        )
                ))
                .deploy(rule);
        ProcessExpressions.registerCallActivityMock("MPAM_QA_CLEARANCE_REQ")
                .onExecutionAddVariable("ClearanceStatus", "RejectDraft")
                .deploy(rule);

        Scenario.run(mpamProcess)
                .startByKey("MPAM")
                .execute();

        verify(mpamProcess, times(1))
                .hasCompleted("CallActivity_QaClearanceReq");


        verify(mpamProcess, times(2))
                .hasCompleted("CallActivity_1rhesrm");

        verify(mpamProcess).hasFinished("EndEvent_MPAM");
    }

    @After
    public void after(){
        RepositoryService repositoryService = rule.getRepositoryService();

        List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId());
        }

        Mocks.reset();
    }
}
