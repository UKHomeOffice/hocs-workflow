package uk.gov.digital.ho.hocs.workflow.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NumberUtilsTest {

    @Test
    public void isNumeric_null(){
        assertThat(NumberUtils.isNumeric(null)).isFalse();
    }

    @Test
    public void isNumeric_blank(){
        assertThat(NumberUtils.isNumeric("")).isFalse();
    }

    @Test
    public void isNumeric_withEmptySpace(){
        assertThat(NumberUtils.isNumeric(" 9")).isFalse();
    }

    @Test
    public void isNumeric_withTrailingSpace(){
        assertThat(NumberUtils.isNumeric("4569 ")).isFalse();
    }

    @Test
    public void isNumeric_withNonNumericChar(){
        assertThat(NumberUtils.isNumeric("a")).isFalse();
    }

    @Test
    public void isNumeric_withMixedChars(){
        assertThat(NumberUtils.isNumeric("345c3")).isFalse();
    }

    @Test
    public void isNumeric_withValidString(){
        assertThat(NumberUtils.isNumeric("98765432")).isTrue();
    }

    @Test
    public void parseInt_withValidString(){
        assertThat(NumberUtils.parseInt("32")).isEqualTo(32);
    }

    @Test
    public void parseInt_withNegativeValue(){
        assertThat(NumberUtils.parseInt("-24")).isEqualTo(-24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInt_withInvalidValue(){
        NumberUtils.parseInt("Hi");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInt_withMinus(){
        NumberUtils.parseInt("-");
    }
}
