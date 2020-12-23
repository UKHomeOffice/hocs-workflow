package uk.gov.digital.ho.hocs.workflow.processes;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.workflow.BpmnService;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:test.properties"})
public class MPAMCreation {

    @MockBean
    BpmnService bpmnService;

    @Test
    @Deployment(resources = "processes/MPAM_CREATION.bpmn")
    public void happyPath() {

        when(bpmnService.caseHasMember(any())).thenReturn(true);

        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("MPAM_CREATION");
        assertThat(processInstance).isStarted();
        assertThat(processInstance).isWaitingAt("UserTask_145n012");

        complete(task(), withVariables("valid", true));

        assertThat(processInstance).isWaitingAt("UserTask_0iez602");

        complete(task(), withVariables("DIRECTION", "FORWARD"));

        assertThat(processInstance)
                .hasPassed("ServiceTask_1wekfef")
                .hasPassed("ServiceTask_0wdqurs")
                .isEnded();

        verify(bpmnService, times(1)).updatePrimaryCorrespondent(any(), any(), any());

        verify(bpmnService, times(1)).updateTeamByStageAndTexts(any(), any(), any(), any(), any(), any(), any());
    }
}
