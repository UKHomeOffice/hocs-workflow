package uk.gov.digital.ho.hocs.workflow.migration.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MigrateCaseRequest {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

}
