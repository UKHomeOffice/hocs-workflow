package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.api.dto.ValidationRuleDto;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor()
@Getter
public class HocsFormValidation {

    protected Map<String, Object> subSchema;

    public static HocsFormValidation from(ValidationRuleDto validationRuleDto) {

        return new HocsFormValidation(validationRuleDto.getSubSchema());
    }
}
