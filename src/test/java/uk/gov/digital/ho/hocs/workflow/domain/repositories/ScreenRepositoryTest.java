package uk.gov.digital.ho.hocs.workflow.domain.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class ScreenRepositoryTest {

    @Autowired
    private ScreenRepository screenRepository;

    @Test
    public void throwNotFoundExceptionWithNonExistentScreen() {
        var screen = screenRepository.getSchema("UNKNOWN", "live");
        assertThat(screen).isNull();
    }

    @Test(expected = ApplicationExceptions.JsonFileReadException.class)
    public void throwFileReadExceptionWithInvalidScreenConfig() {
        screenRepository.getSchema("INVALID_SCREEN", "live");
    }

    @Test
    public void shouldReadValidScreen() {
        var screen = screenRepository.getSchema("TEST_SCREEN", "live");

        assertThat(screen.getTitle()).isEqualTo("Test Screen Title");
        assertThat(screen.getDefaultActionLabel()).isEqualTo("Continue");
        assertThat(screen.getFields()).hasSize(1);
        assertThat(screen.getProps()).isNotNull();
        assertThat(screen.getValidation()).isNotNull();
        assertThat(screen.getSummary()).isNotNull();
    }

    @Test
    public void shouldTrimScreenNameWhitespace() {
        var screen = screenRepository.getSchema(" TEST_SCREEN ", "live");

        assertThat(screen.getTitle()).isEqualTo("Test Screen Title");
    }

    @Test
    public void shouldReadValidScreenWithFolderNameLowercase() {
        var screen = screenRepository.getSchema("TEST_SCREEN", "live");

        assertThat(screen).isNotNull();
    }

    @Test
    public void shouldReadValidScreenWithFolderNameUppercase() {
        var screen = screenRepository.getSchema("TEST_SCREEN", "LIVE");

        assertThat(screen).isNotNull();
    }

    @Test
    public void shouldReadValidScreenWithFolderNameWhitespace() {
        var screen = screenRepository.getSchema(" TEST_SCREEN ", " LIVE ");

        assertThat(screen).isNotNull();
    }

    @Test
    public void shouldReturnNullIfFolderIsInvalid() {
        var screen = screenRepository.getSchema("TEST_SCREEN", "UNKNOWN");

        assertThat(screen).isNull();
    }

}
