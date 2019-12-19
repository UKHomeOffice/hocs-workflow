package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.CaseDetailsFieldDto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class HocsFormField {

    protected String component;

    protected String[] validation;

    protected Map<String, Object> props;

    public static HocsFormField from(FieldDto fieldDto) {
        Map<String, Object> props = fieldDto.getProps();
        props.put("label", fieldDto.getLabel());
        props.put("name", fieldDto.getName());

        return new HocsFormField(fieldDto.getComponent(), fieldDto.getValidation(), props);
    }

    public static HocsFormField from(CaseDetailsFieldDto caseDetailsFieldDto) {
        Map<String, Object> props = caseDetailsFieldDto.getProps();
        props.put("name", caseDetailsFieldDto.getName());
        props.put("disabled", true);
        return new HocsFormField(caseDetailsFieldDto.getComponent(), null, props);
    }

    public static HocsFormField fromTitle(String title) {
        Map<String, Object> props = new HashMap<>();
        props.put("label", title);
        props.put("name", UUID.randomUUID());

        return new HocsFormField("text", new String[0], props);
    }
}
