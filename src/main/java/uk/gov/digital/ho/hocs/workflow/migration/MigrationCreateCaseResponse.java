package uk.gov.digital.ho.hocs.workflow.migration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor()
@Getter
public class MigrationCreateCaseResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    @JsonProperty("reference")
    private final String reference;

    @JsonProperty("seedData")
    private final Map<String, String> seedData;
}
