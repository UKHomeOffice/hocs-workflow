package uk.gov.digital.ho.hocs.migration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class UpdateActiveStageTeamRequest {
    final private String teamUUID;
}
