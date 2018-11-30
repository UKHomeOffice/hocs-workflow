package uk.gov.digital.ho.hocs.workflow.domain.model;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class HocsCaseUUIDTest {

    @Test
    public void shouldRoundtrip() {

        for(CaseDataType caseDataType : CaseDataType.values()) {
            UUID caseUUID = HocsCaseUUID.randomUUID(caseDataType);

            CaseDataType convertedType = HocsCaseUUID.getCaseDataType(caseUUID);
            assertThat(convertedType).isEqualTo(caseDataType);
        }
    }

    @Test
    public void shouldReturnNullOnInvalid() {

        UUID caseUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        CaseDataType convertedType = HocsCaseUUID.getCaseDataType(caseUUID);
        assertThat(convertedType).isEqualTo(null);

    }
}