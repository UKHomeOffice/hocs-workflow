package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HocsFormField {

    @Getter
    public String component;

    public String[] validation;

    @Getter
    public Map<String,Object> props;

    public static HocsFormField from(FieldDto fieldDto) {
        Map<String, Object> props = fieldDto.getProps();
        props.put("label", fieldDto.getLabel());
        props.put("name", fieldDto.getName());

        return new HocsFormField(fieldDto.getComponent(), fieldDto.getValidation(), props);
    }

    public static HocsFormField fromTitle(String title) {
        Map<String, Object> props = new HashMap<>();
        props.put("label", title);
        props.put("name", UUID.randomUUID());

        return new HocsFormField("text", new String[0], props);
    }
}
