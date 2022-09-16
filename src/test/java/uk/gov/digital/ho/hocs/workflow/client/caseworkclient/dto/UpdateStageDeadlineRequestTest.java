package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateStageDeadlineRequestTest {

    @Test
    public void getCreateCaseRequest() {

        UpdateStageDeadlineRequest request = new UpdateStageDeadlineRequest("stageType", 7);

        assertThat(request.getStageType()).isEqualTo("stageType");
        assertThat(request.getDays()).isEqualTo(7);
    }

}
