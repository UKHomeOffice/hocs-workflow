package uk.gov.digital.ho.hocs.workflow.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class MigrationUpdateStageUserRequest {

    @JsonProperty("userUUID")
    private UUID userUUID;

}
