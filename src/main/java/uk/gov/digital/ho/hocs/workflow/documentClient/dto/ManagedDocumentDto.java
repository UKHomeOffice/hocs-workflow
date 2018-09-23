package uk.gov.digital.ho.hocs.workflow.documentClient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.workflow.documentClient.model.ManagedDocumentStatus;
import uk.gov.digital.ho.hocs.workflow.documentClient.model.ManagedDocumentType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Slf4j
public class ManagedDocumentDto {

    @JsonProperty("UUID")
    private UUID uuid;

    @JsonProperty("externalReferenceUUID")
    private UUID externalReferenceUUID;

    @JsonProperty("type")
    private ManagedDocumentType type;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("status")
    private ManagedDocumentStatus status;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("updated")
    private LocalDateTime updated;

    @JsonProperty("expires")
    private LocalDate expires;

    @JsonProperty("deleted")
    private Boolean deleted;

}