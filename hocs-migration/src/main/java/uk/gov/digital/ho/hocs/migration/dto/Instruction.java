package uk.gov.digital.ho.hocs.migration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Instruction {
    private String[] sourceActivityIds;
    private String[] targetActivityIds;
    private String updateEventTrigger;
}
