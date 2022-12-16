package uk.gov.digital.ho.hocs.workflow.client.auditclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.client.auditclient.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateAuditRequest {

    @JsonProperty(value = "correlation_id", required = true)
    private String correlationID;

    @JsonProperty(value = "caseUUID", required = true)
    private UUID caseUUID;

    @JsonProperty(value = "stageUUID")
    private UUID stageUUID;

    @JsonProperty(value = "raising_service", required = true)
    private String raisingService;

    @JsonProperty(value = "audit_payload")
    private String auditPayload;

    @JsonProperty(value = "data")
    private String data;

    @JsonProperty(value = "namespace", required = true)
    private String namespace;

    @JsonProperty(value = "audit_timestamp", required = true)
    private LocalDateTime auditTimestamp;

    @JsonProperty(value = "type", required = true)
    private EventType type;

    @JsonProperty(value = "user_id", required = true)
    private String userID;

}
