package uk.gov.digital.ho.hocs.workflow.util;

import lombok.*;

@Getter
@AllArgsConstructor()
@EqualsAndHashCode
@ToString
public class CallActivityVariable {

    private final String source;

    private final String target;

}
