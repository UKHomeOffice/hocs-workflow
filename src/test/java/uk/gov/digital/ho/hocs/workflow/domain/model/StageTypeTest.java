package uk.gov.digital.ho.hocs.workflow.domain.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static uk.gov.digital.ho.hocs.workflow.domain.model.StageType.*;
import static uk.gov.digital.ho.hocs.workflow.domain.model.StageType.DCU_DTEN_COPY_NUMBER_TEN;
import static uk.gov.digital.ho.hocs.workflow.domain.model.StageType.DCU_DTEN_DISPATCH;

public class StageTypeTest {

    @Test
    public void getDisplayValue() {
        assertThat(DCU_MIN_DATA_INPUT.getDisplayValue()).isEqualTo("DCU_MIN_DATA_INPUT");
        assertThat(DCU_MIN_MARKUP.getDisplayValue()).isEqualTo("DCU_MIN_MARKUP");
        assertThat(DCU_MIN_TRANSFER_CONFIRMATION.getDisplayValue()).isEqualTo("DCU_MIN_TRANSFER_CONFIRMATION");
        assertThat(DCU_MIN_NO_REPLY_NEEDED_CONFIRMATION.getDisplayValue()).isEqualTo("DCU_MIN_NO_REPLY_NEEDED_CONFIRMATION");
        assertThat(DCU_MIN_INITIAL_DRAFT.getDisplayValue()).isEqualTo("DCU_MIN_INITIAL_DRAFT");
        assertThat(DCU_MIN_QA_RESPONSE.getDisplayValue()).isEqualTo("DCU_MIN_QA_RESPONSE");
        assertThat(DCU_MIN_PRIVATE_OFFICE.getDisplayValue()).isEqualTo("DCU_MIN_PRIVATE_OFFICE");
        assertThat(DCU_MIN_MINISTER_SIGN_OFF.getDisplayValue()).isEqualTo("DCU_MIN_MINISTER_SIGN_OFF");
        assertThat(DCU_MIN_DISPATCH.getDisplayValue()).isEqualTo("DCU_MIN_DISPATCH");
        assertThat(DCU_MIN_COPY_NUMBER_TEN.getDisplayValue()).isEqualTo("DCU_MIN_COPY_NUMBER_TEN");
        assertThat(DCU_TRO_DATA_INPUT.getDisplayValue()).isEqualTo("DCU_TRO_DATA_INPUT");
        assertThat(DCU_TRO_MARKUP.getDisplayValue()).isEqualTo("DCU_TRO_MARKUP");
        assertThat(DCU_TRO_TRANSFER_CONFIRMATION.getDisplayValue()).isEqualTo("DCU_TRO_TRANSFER_CONFIRMATION");
        assertThat(DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION.getDisplayValue()).isEqualTo("DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION");
        assertThat(DCU_TRO_INITIAL_DRAFT.getDisplayValue()).isEqualTo("DCU_TRO_INITIAL_DRAFT");
        assertThat(DCU_TRO_QA_RESPONSE.getDisplayValue()).isEqualTo("DCU_TRO_QA_RESPONSE");
        assertThat(DCU_TRO_DISPATCH.getDisplayValue()).isEqualTo("DCU_TRO_DISPATCH");
        assertThat(DCU_TRO_COPY_NUMBER_TEN.getDisplayValue()).isEqualTo("DCU_TRO_COPY_NUMBER_TEN");
        assertThat(DCU_DTEN_DATA_INPUT.getDisplayValue()).isEqualTo("DCU_DTEN_DATA_INPUT");
        assertThat(DCU_DTEN_MARKUP.getDisplayValue()).isEqualTo("DCU_DTEN_MARKUP");
        assertThat(DCU_DTEN_TRANSFER_CONFIRMATION.getDisplayValue()).isEqualTo("DCU_DTEN_TRANSFER_CONFIRMATION");
        assertThat(DCU_DTEN_NO_REPLY_NEEDED_CONFIRMATION.getDisplayValue()).isEqualTo("DCU_DTEN_NO_REPLY_NEEDED_CONFIRMATION");
        assertThat(DCU_DTEN_INITIAL_DRAFT.getDisplayValue()).isEqualTo("DCU_DTEN_INITIAL_DRAFT");
        assertThat(DCU_DTEN_QA_RESPONSE.getDisplayValue()).isEqualTo("DCU_DTEN_QA_RESPONSE");
        assertThat(DCU_DTEN_PRIVATE_OFFICE.getDisplayValue()).isEqualTo("DCU_DTEN_PRIVATE_OFFICE");
        assertThat(DCU_DTEN_DISPATCH.getDisplayValue()).isEqualTo("DCU_DTEN_DISPATCH");
        assertThat(DCU_DTEN_COPY_NUMBER_TEN.getDisplayValue()).isEqualTo("DCU_DTEN_COPY_NUMBER_TEN");

    }

    @Test
    public void shouldNotAccidentallyChangeTheOrder() {
        assertOrderValue(DCU_MIN_DATA_INPUT, 0);
        assertOrderValue(DCU_MIN_MARKUP, 1);
        assertOrderValue(DCU_MIN_TRANSFER_CONFIRMATION, 2);
        assertOrderValue(DCU_MIN_NO_REPLY_NEEDED_CONFIRMATION, 3);
        assertOrderValue(DCU_MIN_INITIAL_DRAFT, 4);
        assertOrderValue(DCU_MIN_QA_RESPONSE, 5);
        assertOrderValue(DCU_MIN_PRIVATE_OFFICE, 6);
        assertOrderValue(DCU_MIN_MINISTER_SIGN_OFF, 7);
        assertOrderValue(DCU_MIN_DISPATCH, 8);
        assertOrderValue(DCU_MIN_COPY_NUMBER_TEN, 9);
        assertOrderValue(DCU_TRO_DATA_INPUT, 10);
        assertOrderValue(DCU_TRO_MARKUP, 11);
        assertOrderValue(DCU_TRO_TRANSFER_CONFIRMATION, 12);
        assertOrderValue(DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION, 13);
        assertOrderValue(DCU_TRO_INITIAL_DRAFT, 14);
        assertOrderValue(DCU_TRO_QA_RESPONSE, 15);
        assertOrderValue(DCU_TRO_DISPATCH, 16);
        assertOrderValue(DCU_TRO_COPY_NUMBER_TEN, 17);
        assertOrderValue(DCU_DTEN_DATA_INPUT, 18);
        assertOrderValue(DCU_DTEN_MARKUP, 19);
        assertOrderValue(DCU_DTEN_TRANSFER_CONFIRMATION, 20);
        assertOrderValue(DCU_DTEN_NO_REPLY_NEEDED_CONFIRMATION, 21);
        assertOrderValue(DCU_DTEN_INITIAL_DRAFT, 22);
        assertOrderValue(DCU_DTEN_QA_RESPONSE, 23);
        assertOrderValue(DCU_DTEN_PRIVATE_OFFICE, 24);
        assertOrderValue(DCU_DTEN_DISPATCH, 25);
        assertOrderValue(DCU_DTEN_COPY_NUMBER_TEN, 26);
    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (StageType stageType : StageType.values()) {
            switch (stageType) {
                case DCU_MIN_DATA_INPUT:
                case DCU_MIN_MARKUP:
                case DCU_MIN_TRANSFER_CONFIRMATION:
                case DCU_MIN_NO_REPLY_NEEDED_CONFIRMATION:
                case DCU_MIN_INITIAL_DRAFT:
                case DCU_MIN_QA_RESPONSE:
                case DCU_MIN_PRIVATE_OFFICE:
                case DCU_MIN_MINISTER_SIGN_OFF:
                case DCU_MIN_DISPATCH:
                case DCU_MIN_COPY_NUMBER_TEN:
                case DCU_TRO_DATA_INPUT:
                case DCU_TRO_MARKUP:
                case DCU_TRO_TRANSFER_CONFIRMATION:
                case DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION:
                case DCU_TRO_INITIAL_DRAFT:
                case DCU_TRO_QA_RESPONSE:
                case DCU_TRO_DISPATCH:
                case DCU_TRO_COPY_NUMBER_TEN:
                case DCU_DTEN_DATA_INPUT:
                case DCU_DTEN_MARKUP:
                case DCU_DTEN_TRANSFER_CONFIRMATION:
                case DCU_DTEN_NO_REPLY_NEEDED_CONFIRMATION:
                case DCU_DTEN_INITIAL_DRAFT:
                case DCU_DTEN_QA_RESPONSE:
                case DCU_DTEN_PRIVATE_OFFICE:
                case DCU_DTEN_DISPATCH:
                case DCU_DTEN_COPY_NUMBER_TEN:
                    break;
                default:
                    fail("You've added a StageType, make sure you've written all the tests!");
            }
        }
    }

    private void assertOrderValue(StageType stageType, int value) {
        assertThat(stageType.ordinal()).isEqualTo(value);
    }
}