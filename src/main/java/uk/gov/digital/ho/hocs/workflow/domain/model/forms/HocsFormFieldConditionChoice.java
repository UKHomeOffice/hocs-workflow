package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HocsFormFieldConditionChoice {
    private String conditionPropertyName;
    private String conditionPropertyValue;
    private List<HocsFormFieldChoice> choices;
}
