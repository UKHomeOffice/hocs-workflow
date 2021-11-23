package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import org.junit.Test;
import uk.gov.digital.ho.hocs.workflow.api.dto.SecondaryActionDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class HocsFormSecondaryActionTest {

    @Test
    public void fromTest() {

        UUID uuid = UUID.randomUUID();
        String name = "testName";
        String label = "testLabel";
        String component = "backButton";
        String[] validation = Arrays.asList("validation1", "validation2").toArray(new String[2]);
        Map<String, Object> props = new HashMap<>();


        SecondaryActionDto dto = new SecondaryActionDto(uuid, name, label, component, validation, props);

        HocsFormSecondaryAction result = HocsFormSecondaryAction.from(dto);

        assertThat(result.getComponent()).isEqualTo(component);
        assertThat(result.getProps().get("label")).isEqualTo(label);
        assertThat(result.getProps().get("name")).isEqualTo(name);
        assertThat(result.getValidation()).isEqualTo(validation);

    }

}
