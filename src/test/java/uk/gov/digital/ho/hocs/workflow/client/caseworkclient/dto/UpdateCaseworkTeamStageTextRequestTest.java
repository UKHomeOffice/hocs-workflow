package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UpdateCaseworkTeamStageTextRequestTest {

    @Test
    public void whenUpdateCaseworkTeamStageTextRequest(){
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String[] texts = { "Text1", "Text2" };

        UpdateCaseworkTeamStageTextRequest request = new UpdateCaseworkTeamStageTextRequest(
                caseUUID, stageUUID, "stageType", "teamUUIDKey", "teamNameKey", texts);

        assertThat(request).isNotNull();
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(request.getStageUUID()).isEqualTo(stageUUID);
        assertThat(request.getStageType()).isEqualTo("stageType");
        assertThat(request.getTeamUUIDKey()).isEqualTo("teamUUIDKey");
        assertThat(request.getTeamNameKey()).isEqualTo("teamNameKey");
        assertThat(request.getTexts()).isEqualTo(texts);
    }
}
