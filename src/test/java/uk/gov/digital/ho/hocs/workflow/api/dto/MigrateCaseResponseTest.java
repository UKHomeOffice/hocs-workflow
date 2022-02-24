package uk.gov.digital.ho.hocs.workflow.api.dto;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrateCaseResponseTest {

    @Test
    public void getMigrateCaseResponse() {

        UUID caseUUID = UUID.randomUUID();
        String reference = "someRef";

        MigrateCaseResponse migrateCaseResponse = new MigrateCaseResponse(caseUUID, reference);

        assertThat(migrateCaseResponse.getUuid()).isEqualTo(caseUUID);
        assertThat(migrateCaseResponse.getReference()).isEqualTo(reference);

    }

    @Test
    public void getCreateCaseResponseNull() {

        MigrateCaseResponse migrateCaseResponse = new MigrateCaseResponse(null, null);

        assertThat(migrateCaseResponse.getUuid()).isEqualTo(null);
        assertThat(migrateCaseResponse.getReference()).isEqualTo(null);

    }
}