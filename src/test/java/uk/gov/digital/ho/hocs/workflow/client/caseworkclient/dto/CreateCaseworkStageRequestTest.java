package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseworkStageRequestTest {

    @Test
    public void getCreateStageRequest() {

        String stageType = "DCU_MIN_MARKUP";
        UUID teamUUID = UUID.randomUUID();
        String allocationType = "anyType";

        CreateCaseworkStageRequest createStageRequest = new CreateCaseworkStageRequest(stageType, teamUUID, allocationType);

        assertThat(createStageRequest.getType()).isEqualTo(stageType);
        assertThat(createStageRequest.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(createStageRequest.getAllocationType()).isEqualTo(allocationType);

    }

    @Test
    public void getCreateStageRequestNull() {

        CreateCaseworkStageRequest createStageRequest = new CreateCaseworkStageRequest(null, null, null);

        assertThat(createStageRequest.getType()).isNull();
        assertThat(createStageRequest.getTeamUUID()).isNull();
        assertThat(createStageRequest.getAllocationType()).isNull();
    }

}