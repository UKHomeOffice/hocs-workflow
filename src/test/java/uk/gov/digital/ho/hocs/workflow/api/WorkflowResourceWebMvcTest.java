package uk.gov.digital.ho.hocs.workflow.api;


import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.hocs.workflow.BpmnService;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetProcessVariablesResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.ProcessVariables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(WorkflowResource.class)
@RunWith(SpringRunner.class)
public class WorkflowResourceWebMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowService workflowService;
    @MockBean
    private BpmnService bpmnService;

    @Test
    public void requestingProcessVariables_returnsTheExpectedJSON() throws Exception {
        UUID caseUUID = UUID.randomUUID();

        when(workflowService.getAllTaskVariablesForCase(caseUUID))
            .thenReturn(buildExampleGetProcessVariablesResponse(caseUUID));

        mockMvc
            .perform(get("/case/%s/process/variables".formatted(caseUUID.toString())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.caseUUID", is(caseUUID.toString())))
            .andExpect(jsonPath("$.stageUUID", nullValue()))
            .andExpect(jsonPath("$.processes[0].processKey", is("process-key")))
            .andExpect(jsonPath("$.processes[0].rootProcessKey", is("root-process-key")))
            .andExpect(jsonPath("$.processes[0].businessKey", is(caseUUID.toString())))
            .andExpect(jsonPath("$.processes[0].variables.present", is("String")))
            .andExpect(jsonPath("$.processes[0].variables.empty", nullValue()));
    }

    private static @NotNull GetProcessVariablesResponse buildExampleGetProcessVariablesResponse(UUID caseUUID) {
        return new GetProcessVariablesResponse(
            caseUUID,
            null,
            List.of(
                new ProcessVariables(
                    "process-key",
                    caseUUID.toString(),
                    "root-process-key",
                    Map.of(
                        "present", Optional.of("String"),
                        "empty", Optional.empty()
                    )
                )
            )
        );
    }

    @Test
    public void puttingProcessVariables_updatesTheVariablesForThatProcess() throws Exception {
        UUID caseUUID = UUID.randomUUID();
        String processKey = "process-key";

        String jsonVariables = """
            {
              "present": "String",
              "empty": null
            }""";

        Map<String, String> expectedVariables = new HashMap<>() {
            {
                put("present", "String");
                put("empty", null);
            }
        };

        mockMvc
            .perform(
                put("/case/%s/process/%s/variables".formatted(caseUUID.toString(), processKey))
                    .contentType(ContentType.APPLICATION_JSON.toString())
                    .content(jsonVariables)
            )
            .andExpect(status().isOk());

        verify(workflowService).updateProcessVariables(eq(processKey), eq(expectedVariables));
    }
}
