package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.workflow.domain.model.StageType;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseworkStageRequestTest {

    @Test
    public void getCreateStageRequest() {

        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();
        LocalDate deadline = LocalDate.now();

        CreateCaseworkStageRequest createStageRequest = new CreateCaseworkStageRequest(stageType, teamUUID, deadline);

        assertThat(createStageRequest.getType()).isEqualTo(stageType);
        assertThat(createStageRequest.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(createStageRequest.getDeadline()).isEqualTo(deadline);

    }

    @Test
    public void getCreateStageRequestNull() {

        CreateCaseworkStageRequest createStageRequest = new CreateCaseworkStageRequest(null, null,  null);

        assertThat(createStageRequest.getType()).isNull();
        assertThat(createStageRequest.getTeamUUID()).isNull();
        assertThat(createStageRequest.getDeadline()).isNull();

    }

}