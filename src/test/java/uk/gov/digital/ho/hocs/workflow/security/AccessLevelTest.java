package uk.gov.digital.ho.hocs.workflow.security;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.digital.ho.hocs.workflow.security.AccessLevel.*;

public class AccessLevelTest {

    @Test
    public void getDisplayValue() {
        assertThat(UNSET.getLevel()).isEqualTo(0);
        assertThat(SUMMARY.getLevel()).isEqualTo(1);
        assertThat(READ.getLevel()).isEqualTo(2);
        assertThat(WRITE.getLevel()).isEqualTo(3);
        assertThat(OWNER.getLevel()).isEqualTo(5);

    }

    @Test
    public void shouldNotAccidentallyChangeTheOrder() {
        assertOrderValue(UNSET, 0);
        assertOrderValue(SUMMARY, 1);
        assertOrderValue(READ, 2);
        assertOrderValue(WRITE, 3);
        assertOrderValue(OWNER, 4);
    }

    @Test
    public void shouldNotAccidentallyAddValues() {
        for (AccessLevel accessLevel : AccessLevel.values()) {
            switch (accessLevel) {
                case UNSET:
                case SUMMARY:
                case READ:
                case WRITE:
                case OWNER:
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