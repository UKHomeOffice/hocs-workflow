package uk.gov.digital.ho.hocs.workflow.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UuidUtilsTest {

    @Test
    public void isUUID_blank(){
        assertThat(UuidUtils.isUUID("")).isFalse();
    }

    @Test
    public void isUUID_null(){
        assertThat(UuidUtils.isUUID(null)).isFalse();
    }

    @Test
    public void isUUID(){
        assertThat(UuidUtils.isUUID("d802a084-30d9-4cdb-b5b0-0a28d0b3eea1")).isTrue();
    }

    @Test
    public void isUUID_tooShort(){
        assertThat(UuidUtils.isUUID("d802a084-30d9-4cdb-b5b0-0a28d0b3eea")).isFalse();
    }

    @Test
    public void isUUID_tooLong(){
        assertThat(UuidUtils.isUUID("d802a084-30d9-4cdb-b5b0-0a28d0b3eea12")).isFalse();
    }

    @Test
    public void isUUID_withSpace(){
        assertThat(UuidUtils.isUUID("d802a084-30d9-4cdb-b5b0-0a28d0b3eea1 ")).isFalse();
    }

}

