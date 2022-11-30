package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.CaseDetailsFieldDto;
import uk.gov.digital.ho.hocs.workflow.domain.repositories.entity.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class HocsFormField {

    protected String component;

    protected Object[] validation;

    protected Map<String, Object> props;

    @Deprecated(forRemoval = true)
    public static HocsFormField from(FieldDto fieldDto) {
        Map<String, Object> props = fieldDto.getProps();
        props.put("label", fieldDto.getLabel());
        props.put("name", fieldDto.getName());

        if (fieldDto.getChild() != null) {
            props.put("child", HocsFormField.from(fieldDto.getChild()));
        }

        return new HocsFormField(fieldDto.getComponent(), fieldDto.getValidation(), props);
    }

    public static HocsFormField from(Field field) {
        Map<String, Object> props = field.getProps();
        props.put("label", field.getLabel());
        props.put("name", field.getName());

        if (field.getChild() != null) {
            props.put("child", HocsFormField.from(field.getChild()));
        }

        return new HocsFormField(field.getComponent(), field.getValidation(), props);
    }

    public static HocsFormField from(CaseDetailsFieldDto caseDetailsFieldDto) {
        Map<String, Object> props = caseDetailsFieldDto.getProps();
        props.put("name", caseDetailsFieldDto.getName());
        props.put("disabled", true);
        return new HocsFormField(caseDetailsFieldDto.getComponent(), null, props);
    }

    @Deprecated(forRemoval = true)
    public static HocsFormField fromTitle(String title) {
        Map<String, Object> props = new HashMap<>();
        props.put("label", title);
        props.put("name", UUID.randomUUID());

        return new HocsFormField("text", new String[0], props);
    }

}
