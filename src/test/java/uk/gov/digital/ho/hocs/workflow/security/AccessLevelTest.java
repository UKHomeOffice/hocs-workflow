package uk.gov.digital.ho.hocs.workflow.security;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.digital.ho.hocs.workflow.security.AccessLevel.*;

public class AccessLevelTest {

    @Test
    public void getDisplayValue() {
        assertThat(UNSET.getLevel()).isEqualTo(0);
        assertThat(MIGRATE.getLevel()).isEqualTo(1);
        assertThat(RESTRICTED_OWNER.getLevel()).isEqualTo(2);
        assertThat(SUMMARY.getLevel()).isEqualTo(3);
        assertThat(READ.getLevel()).isEqualTo(4);
        assertThat(WRITE.getLevel()).isEqualTo(5);
        assertThat(OWNER.getLevel()).isEqualTo(6);
        assertThat(CASE_ADMIN.getLevel()).isEqualTo(7);

    }

    @Test
    public void shouldNotAccidentallyChangeTheOrder() {
        assertOrderValue(UNSET, 0);
        assertOrderValue(MIGRATE, 1);
        assertOrderValue(RESTRICTED_OWNER, 2);
        assertOrderValue(SUMMARY, 3);
        assertOrderValue(READ, 4);
        assertOrderValue(WRITE, 5);
        assertOrderValue(OWNER, 6);
        assertOrderValue(CASE_ADMIN, 7);
    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (AccessLevel accessLevel : AccessLevel.values()) {
            switch (accessLevel) {
                case UNSET:
                case MIGRATE:
                case SUMMARY:
                case READ:
                case WRITE:
                case OWNER:
                case CASE_ADMIN:
                    break;
                default:
                    fail("You've added a AccessLevel, make sure you've written all the tests!");
            }
        }
    }

    private void assertOrderValue(AccessLevel accessLevel, int value) {
        assertThat(accessLevel.ordinal()).isEqualTo(value);
    }

}