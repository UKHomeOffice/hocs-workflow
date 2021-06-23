package uk.gov.digital.ho.hocs.migration;

import lombok.Getter;
import lombok.Setter;

public class UpdateProcessVariableRequest {
    @Getter
    @Setter
    private String type;
    @Getter
    @Setter
    private String value;
    @Getter
    private Object valueInfo;
}
