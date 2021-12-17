package uk.gov.digital.ho.hocs.workflow.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor()
@EqualsAndHashCode
@ToString
public class CallActivityVariable {
    private final String source;
    private final String target;
}
