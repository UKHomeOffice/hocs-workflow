package uk.gov.digital.ho.hocs.workflow.domain.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType.*;

public class CaseDataTypeTest {


    @Test
    public void getDisplayName() {
        assertThat(MIN.getDisplayCode()).isEqualTo("MIN");
        assertThat(TRO.getDisplayCode()).isEqualTo("TRO");
        assertThat(DTEN.getDisplayCode()).isEqualTo("DTEN");
    }

    @Test
    public void getValue() {
        assertThat(MIN.getValue()).isEqualTo("a1");
        assertThat(TRO.getValue()).isEqualTo("a2");
        assertThat(DTEN.getValue()).isEqualTo("a3");
    }

    @Test
    public void shouldNotAccidentallyChangeTheOrder() {
        assertOrderValue(MIN, 0);
        assertOrderValue(TRO, 1);
        assertOrderValue(DTEN, 2);
    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (CaseDataType caseDataType : CaseDataType.values()) {
            switch (caseDataType) {
                case MIN:
                case TRO:
                case DTEN:
                    break;
                default:
                    fail("You've added a CaseDataType, make sure you've written all the tests!");
            }
        }
    }

    private void assertOrderValue(CaseDataType caseDataType, int value) {
        assertThat(caseDataType.ordinal()).isEqualTo(value);
    }
}