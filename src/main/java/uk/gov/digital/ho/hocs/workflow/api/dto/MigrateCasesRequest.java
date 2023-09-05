package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MigrateCasesRequest {

    @JsonProperty("caseUUIDs")
    private List<UUID> caseUUIDs;

    @JsonProperty("sourceVersion")
    private String sourceVersion;

    @JsonProperty("targetVersion")
    private String targetVersion;

}
