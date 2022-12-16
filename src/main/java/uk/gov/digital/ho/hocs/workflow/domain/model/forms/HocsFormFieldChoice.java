package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HocsFormFieldChoice {
    private String value;
    private String label;
}
