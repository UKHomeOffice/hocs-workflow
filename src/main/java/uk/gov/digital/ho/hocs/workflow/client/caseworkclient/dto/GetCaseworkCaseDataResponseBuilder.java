package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public final class GetCaseworkCaseDataResponseBuilder {
    private UUID uuid;
    private ZonedDateTime created;
    private String type;
    private String reference;
    private Map<String, String> data;
    private LocalDate caseDeadline;
    private LocalDate dateReceived;
    private UUID primaryTopicUUID;
    private GetTopicResponse primaryTopic;
    private UUID primaryCorrespondentUUID;
    private GetCorrespondentResponse primaryCorrespondent;
    private Boolean completed;


    private GetCaseworkCaseDataResponseBuilder() {
    }

    public static GetCaseworkCaseDataResponseBuilder aGetCaseworkCaseDataResponse() {
        return new GetCaseworkCaseDataResponseBuilder();
    }

    public GetCaseworkCaseDataResponseBuilder withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withCreated(ZonedDateTime created) {
        this.created = created;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withData(Map<String, String> data) {
        this.data = data;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withCaseDeadline(LocalDate caseDeadline) {
        this.caseDeadline = caseDeadline;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withDateReceived(LocalDate dateReceived) {
        this.dateReceived = dateReceived;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withPrimaryTopicUUID(UUID primaryTopicUUID) {
        this.primaryTopicUUID = primaryTopicUUID;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withPrimaryTopic(GetTopicResponse primaryTopic) {
        this.primaryTopic = primaryTopic;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withPrimaryCorrespondentUUID(UUID primaryCorrespondentUUID) {
        this.primaryCorrespondentUUID = primaryCorrespondentUUID;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withPrimaryCorrespondent(GetCorrespondentResponse primaryCorrespondent) {
        this.primaryCorrespondent = primaryCorrespondent;
        return this;
    }

    public GetCaseworkCaseDataResponseBuilder withCompleted(Boolean completed) {
        this.completed = completed;
        return this;
    }

    public GetCaseworkCaseDataResponse build() {
        return new GetCaseworkCaseDataResponse(uuid, created, type, reference, data, caseDeadline, dateReceived, primaryTopicUUID, primaryTopic, primaryCorrespondentUUID, primaryCorrespondent, completed);
    }
}
