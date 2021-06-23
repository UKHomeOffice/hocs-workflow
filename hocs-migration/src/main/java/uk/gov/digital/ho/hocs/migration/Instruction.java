package uk.gov.digital.ho.hocs.migration;

import lombok.Getter;

@Getter
public class Instruction {
    private String[] sourceActivityIds;
    private String[] targetActivityIds;
    private String updateEventTrigger;
}
