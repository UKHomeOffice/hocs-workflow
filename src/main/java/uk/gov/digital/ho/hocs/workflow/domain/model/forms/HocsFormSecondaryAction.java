package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.workflow.api.dto.SecondaryActionDto;

import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class HocsFormSecondaryAction {

    private String component;

    private String[] validation;

    private Map<String,Object> props;

    public static HocsFormSecondaryAction from(SecondaryActionDto secondaryActionDto) {
        Map<String, Object> props = secondaryActionDto.getProps();
        props.put("label", secondaryActionDto.getLabel());
        props.put("name", secondaryActionDto.getName());

        return new HocsFormSecondaryAction(secondaryActionDto.getComponent(), secondaryActionDto.getValidation(), props);
    }
}
