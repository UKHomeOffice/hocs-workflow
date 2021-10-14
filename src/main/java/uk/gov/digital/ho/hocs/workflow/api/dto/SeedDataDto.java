package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Getter
@NoArgsConstructor
@Builder
public class SeedDataDto {

    @NonNull
    @JsonProperty(value = "DateReceived", required = true)
    String dateReceived;

    @NonNull
    @JsonProperty(value = "LastUpdatedByUserUUID", required = true)
    String lastUpdatedByUserUUID;

    @NonNull
    @JsonProperty(value = "CaseReference", required = true)
    String caseReference;

    @JsonProperty("Topics")
    String topics;

    @JsonProperty("RequestQuestion")
    String requestQuestion;

    @JsonProperty("KimuDateReceived")
    String kimuDateReceived;

    @JsonProperty("OriginalChannel")
    String originalChannel;
}
