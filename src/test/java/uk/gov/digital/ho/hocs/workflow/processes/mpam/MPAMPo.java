package uk.gov.digital.ho.hocs.workflow.processes.mpam;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRule;
import org.camunda.community.process_test_coverage.junit4.platform7.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import java.util.List;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.withVariables;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Deployment(resources = "processes/MPAM/MPAM_PO.bpmn")
public class MPAMPo {

    @Rule
    @ClassRule
    public static TestCoverageProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create()
            .assertClassCoverageAtLeast(0.2)
            .build();

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
    public void whenRejectDraft_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam_whenBusAreaUkvi() {

        Mocks.register("BusArea", "UKVI");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Reject-PfS",
                        "BusArea", "UKVI")));
        when(processScenario.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectPfs", "Casenote Reject",
                        "BusArea", "UKVI")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("Screen_Reject");
        verify(processScenario).hasCompleted("Service_SaveRejectPfsNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByPo");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By PO"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForQA");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_QA"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenRejectDraft_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam_whenBusAreaBf() {

        Mocks.register("BusArea", "BF");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Reject-PfS",
                        "BusArea", "BF")));
        when(processScenario.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectPfs", "Casenote Reject",
                        "BusArea", "BF")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("Screen_Reject");
        verify(processScenario).hasCompleted("Service_SaveRejectPfsNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByPo");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By PO"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForQA");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_QA"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenRejectDraft_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam_whenBusAreaIe() {

        Mocks.register("BusArea", "IE");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Reject-PfS",
                        "BusArea", "IE")));
        when(processScenario.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectPfs", "Casenote Reject",
                        "BusArea", "IE")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("Screen_Reject");
        verify(processScenario).hasCompleted("Service_SaveRejectPfsNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByPo");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By PO"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForQA");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_QA"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenRejectDraft_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam_whenBusAreaEuss() {

        Mocks.register("BusArea", "EUSS");

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Reject-PfS",
                        "BusArea", "EUSS")));
        when(processScenario.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectPfs", "Casenote Reject",
                        "BusArea", "EUSS")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("Screen_Reject");
        verify(processScenario).hasCompleted("Service_SaveRejectPfsNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByPo");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By PO"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForDraft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenRejectDraft_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam_whenBusAreaHmpo() {

        Mocks.register("BusArea", "HMPO");

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Reject-PfS",
                        "BusArea", "HMPO")));
        when(processScenario.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectPfs", "Casenote Reject",
                        "BusArea", "HMPO")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("Screen_Reject");
        verify(processScenario).hasCompleted("Service_SaveRejectPfsNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByPo");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By PO"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForDraft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenRejectDraft_thenSavesCasenote_andUpdatesRejected_andUpdatesTeam_whenBusAreaWindrush() {

        Mocks.register("BusArea", "Windrush");

        when(processScenario.waitsAtUserTask("Validate_UserInput"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Reject-PfS",
                        "BusArea", "Windrush")));
        when(processScenario.waitsAtUserTask("Validate_Reject"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "CaseNote_RejectPfs", "Casenote Reject",
                        "BusArea", "Windrush")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("Screen_Reject");
        verify(processScenario).hasCompleted("Service_SaveRejectPfsNote");
        verify(bpmnService).updateAllocationNote(any(), any(), eq("Casenote Reject"), eq("REJECT"));
        verify(processScenario).hasCompleted("Service_UpdateToRejectedByPo");
        verify(bpmnService).updateValue(any(), any(), eq("Rejected"), eq("By PO"));
        verify(processScenario).hasCompleted("Service_UpdateTeamForDraft");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_DRAFT"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenApprovedMinisterialDispatch_whenBusAreaUkvi() {

        Mocks.register("BusArea", "UKVI");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Approved-Ministerial-Dispatch",
                        "BusArea", "UKVI")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("UpdateTeamAwaitDispatch");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_PO_APPROVED_MIN_DISPATCH"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenApprovedMinisterialDispatch_whenBusAreaBf() {

        Mocks.register("BusArea", "BF");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Approved-Ministerial-Dispatch",
                        "BusArea", "BF")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("UpdateTeamAwaitDispatch");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_PO_APPROVED_MIN_DISPATCH"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenApprovedMinisterialDispatch_whenBusAreaIe() {

        Mocks.register("BusArea", "IE");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Approved-Ministerial-Dispatch",
                        "BusArea", "IE")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("UpdateTeamAwaitDispatch");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_PO_APPROVED_MIN_DISPATCH"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenApprovedLocalDispatch_whenBusAreaUkvi() {

        Mocks.register("BusArea", "UKVI");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Approved-Local-Dispatch",
                        "BusArea", "UKVI")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("UpdateTeamAwaitDispatch");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_PO_APPROVED_MIN_DISPATCH"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenApprovedLocalDispatch_whenBusAreaBf() {

        Mocks.register("BusArea", "BF");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Approved-Local-Dispatch",
                        "BusArea", "BF")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("UpdateTeamAwaitDispatch");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_PO_APPROVED_MIN_DISPATCH"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @Test
    public void whenApprovedLocalDispatch_whenBusAreaIe() {

        Mocks.register("BusArea", "IE");

        when(processScenario.waitsAtUserTask("Validate_UserInputUkviBfIe"))
                .thenReturn(task -> task.complete(withVariables(
                        "valid", true,
                        "DIRECTION", "FORWARD",
                        "PoStatus", "Approved-Local-Dispatch",
                        "BusArea", "IE")));

        Scenario.run(processScenario)
                .startByKey("MPAM_PO")
                .execute();

        verify(processScenario).hasCompleted("UpdateTeamAwaitDispatch");
        verify(bpmnService).updateTeamByStageAndTexts(any(), any(), eq("MPAM_PO_APPROVED_MIN_DISPATCH"), eq("QueueTeamUUID"), eq("QueueTeamName"), eq("BusArea"), eq("RefType"));
        verify(processScenario).hasFinished("EndEvent_MpamPo");
    }

    @After
    public void after() {
        RepositoryService repositoryService = rule.getRepositoryService();

        List<org.camunda.bpm.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.camunda.bpm.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId());
        }

        Mocks.reset();
    }
}
