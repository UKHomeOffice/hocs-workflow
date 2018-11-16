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
        assertThat(DCU_MIN_DATA_INPUT_QA.getDisplayValue()).isEqualTo("DCU_MIN_DATA_INPUT_QA");
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
        assertThat(DCU_TRO_DATA_INPUT_QA.getDisplayValue()).isEqualTo("DCU_TRO_DATA_INPUT_QA");
        assertThat(DCU_TRO_MARKUP.getDisplayValue()).isEqualTo("DCU_TRO_MARKUP");
        assertThat(DCU_TRO_TRANSFER_CONFIRMATION.getDisplayValue()).isEqualTo("DCU_TRO_TRANSFER_CONFIRMATION");
        assertThat(DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION.getDisplayValue()).isEqualTo("DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION");
        assertThat(DCU_TRO_INITIAL_DRAFT.getDisplayValue()).isEqualTo("DCU_TRO_INITIAL_DRAFT");
        assertThat(DCU_TRO_QA_RESPONSE.getDisplayValue()).isEqualTo("DCU_TRO_QA_RESPONSE");
        assertThat(DCU_TRO_DISPATCH.getDisplayValue()).isEqualTo("DCU_TRO_DISPATCH");
        assertThat(DCU_TRO_COPY_NUMBER_TEN.getDisplayValue()).isEqualTo("DCU_TRO_COPY_NUMBER_TEN");
        assertThat(DCU_DTEN_DATA_INPUT.getDisplayValue()).isEqualTo("DCU_DTEN_DATA_INPUT");
        assertThat(DCU_DTEN_DATA_INPUT_QA.getDisplayValue()).isEqualTo("DCU_DTEN_DATA_INPUT_QA");
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
        assertOrderValue(DCU_MIN_DATA_INPUT_QA, 1);
        assertOrderValue(DCU_MIN_MARKUP, 2);
        assertOrderValue(DCU_MIN_TRANSFER_CONFIRMATION, 3);
        assertOrderValue(DCU_MIN_NO_REPLY_NEEDED_CONFIRMATION, 4);
        assertOrderValue(DCU_MIN_INITIAL_DRAFT, 5);
        assertOrderValue(DCU_MIN_QA_RESPONSE, 6);
        assertOrderValue(DCU_MIN_PRIVATE_OFFICE, 7);
        assertOrderValue(DCU_MIN_MINISTER_SIGN_OFF, 8);
        assertOrderValue(DCU_MIN_DISPATCH, 9);
        assertOrderValue(DCU_MIN_COPY_NUMBER_TEN, 10);
        assertOrderValue(DCU_TRO_DATA_INPUT, 11);
        assertOrderValue(DCU_TRO_DATA_INPUT_QA, 12);
        assertOrderValue(DCU_TRO_MARKUP, 13);
        assertOrderValue(DCU_TRO_TRANSFER_CONFIRMATION, 14);
        assertOrderValue(DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION, 15);
        assertOrderValue(DCU_TRO_INITIAL_DRAFT, 16);
        assertOrderValue(DCU_TRO_QA_RESPONSE, 17);
        assertOrderValue(DCU_TRO_DISPATCH, 18);
        assertOrderValue(DCU_TRO_COPY_NUMBER_TEN, 19);
        assertOrderValue(DCU_DTEN_DATA_INPUT, 20);
        assertOrderValue(DCU_DTEN_DATA_INPUT_QA, 21);
        assertOrderValue(DCU_DTEN_MARKUP, 22);
        assertOrderValue(DCU_DTEN_TRANSFER_CONFIRMATION, 23);
        assertOrderValue(DCU_DTEN_NO_REPLY_NEEDED_CONFIRMATION, 24);
        assertOrderValue(DCU_DTEN_INITIAL_DRAFT, 25);
        assertOrderValue(DCU_DTEN_QA_RESPONSE, 26);
        assertOrderValue(DCU_DTEN_PRIVATE_OFFICE, 27);
        assertOrderValue(DCU_DTEN_DISPATCH, 28);
        assertOrderValue(DCU_DTEN_COPY_NUMBER_TEN, 29);
    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (StageType stageType : StageType.values()) {
            switch (stageType) {
                case DCU_MIN_DATA_INPUT:
                case DCU_MIN_DATA_INPUT_QA:
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
                case DCU_TRO_DATA_INPUT_QA:
                case DCU_TRO_MARKUP:
                case DCU_TRO_TRANSFER_CONFIRMATION:
                case DCU_TRO_NO_REPLY_NEEDED_CONFIRMATION:
                case DCU_TRO_INITIAL_DRAFT:
                case DCU_TRO_QA_RESPONSE:
                case DCU_TRO_DISPATCH:
                case DCU_TRO_COPY_NUMBER_TEN:
                case DCU_DTEN_DATA_INPUT:
                case DCU_DTEN_DATA_INPUT_QA:
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