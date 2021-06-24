package uk.gov.digital.ho.hocs.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Instruction {
    final private String[] sourceActivityIds;
    final private String[] targetActivityIds;
    final private String updateEventTrigger;
}
