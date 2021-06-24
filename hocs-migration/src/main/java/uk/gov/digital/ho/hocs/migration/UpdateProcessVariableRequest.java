package uk.gov.digital.ho.hocs.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UpdateProcessVariableRequest {
    final private String type;
    final private String value;
    final private Object valueInfo;
}
