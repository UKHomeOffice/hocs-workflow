package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.FieldDto;

import java.io.IOException;
import java.util.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HocsFormField {

    public String component;

    public String[] validation;

    public Map<String,Object> props;

    public static HocsFormField from(FieldDto fieldDto) {
        Map<String, Object> props = fieldDto.getProps();
        props.put("label", fieldDto.getLabel());
        props.put("name", fieldDto.getName());

        return new HocsFormField(fieldDto.getComponent(), fieldDto.getValidation(), props);
    }
}
