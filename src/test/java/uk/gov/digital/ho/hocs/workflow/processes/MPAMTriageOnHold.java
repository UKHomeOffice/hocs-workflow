package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:test.properties"})
public class MPAMTriageOnHold {

    @MockBean
    BpmnService bpmnService;

    @Test
    public void happyPath() {

        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("MPAM_TRIAGE_ON_HOLD");
        assertThat(processInstance).isStarted();

        assertThat(processInstance).isWaitingAt("UserTask_0jxw8et");

        complete(task(), withVariables("valid", true, "TriageOnHoldOutcome", "PutOnCampaign", "DIRECTION", "FORWARD"));

        assertThat(processInstance).isWaitingAt("UserTask_1ql7p2r");

        complete(task(), withVariables("valid", true, "DIRECTION", "FORWARD"));

        assertThat(processInstance)
                .hasPassed("ServiceTask_1smdf47")
                .hasPassed("ServiceTask_18u5rz3")
                .hasPassed("ServiceTask_0ms9mqv")
                .isEnded();

        verify(bpmnService, times(1)).blankCaseValues(any(), any(), any());

        verify(bpmnService, times(1)).updateTeamByStageAndTexts(any(), any(), any(), any(), any(), any(), any());

    }
}
