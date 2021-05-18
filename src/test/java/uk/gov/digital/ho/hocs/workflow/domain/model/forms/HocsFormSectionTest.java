package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDtoBuilder;

import java.util.HashMap;
import java.util.Map;

public class HocsFormSectionTest {

    @Test
    public void shouldSetName() {
        Map<String, Object> propertyA = new HashMap<>(Map.of("testprop", "prop"));
        FieldDto fieldDto = FieldDtoBuilder.aFieldDto().withName("testName").withLabel("label").withComponent("componentA").withProps(propertyA).build();
        HocsFormField field = HocsFormField.from(fieldDto);

        HocsFormSection section = new HocsFormSection(field);

        assertThat(section.name).isEqualTo("testName");
    }
}
