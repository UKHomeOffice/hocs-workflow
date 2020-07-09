package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class UpdateDeadlineForStagesRequestTest {

    @Test
    public void testUpdateDeadlineForStagesRequest() {

        Map<String, Integer> stageTypeAndDaysMap = Map.of("a_stage_type", 10);
        UpdateDeadlineForStagesRequest request =
                new UpdateDeadlineForStagesRequest(stageTypeAndDaysMap);

        assertThat(request.getStageTypeAndDaysMap()).isEqualTo(stageTypeAndDaysMap);
    }
}
