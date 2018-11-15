package uk.gov.digital.ho.hocs.workflow.dto;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseResponseTest {

    @Test
    public void getCreateCaseResponse() {

        UUID caseUUID = UUID.randomUUID();
        String reference = "someRef";

        CreateCaseResponse createCaseResponse = new CreateCaseResponse(caseUUID, reference);

        assertThat(createCaseResponse.getUuid()).isEqualTo(caseUUID);
        assertThat(createCaseResponse.getReference()).isEqualTo(reference);

    }

    @Test
    public void getCreateCaseResponseNull() {

        CreateCaseResponse createCaseResponse = new CreateCaseResponse(null, null);

        assertThat(createCaseResponse.getUuid()).isEqualTo(null);
        assertThat(createCaseResponse.getReference()).isEqualTo(null);

    }
}