package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor()
@NoArgsConstructor
@Getter
public class StageDto {

    public StageDto(UUID uuid, String type) {
        this.uuid = uuid;
        this.type = type;
    }

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("deadlineWarning")
    private LocalDate deadlineWarning;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("caseType")
    private String type;

    @JsonProperty("transitionNote")
    private UUID transitionNoteUUID;

    @JsonRawValue
    private String correspondents;

    @JsonProperty("caseCreated")
    private LocalDateTime caseCreated;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("assignedTopic")
    private String assignedTopic;
}
