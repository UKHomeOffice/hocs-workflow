package uk.gov.digital.ho.hocs.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class DocumentData {

    @JsonProperty("displayName")
    private String documentDisplayName;

    @JsonProperty("type")
    private DocumentType documentType;

    @JsonProperty("s3UntrustedUrl")
    private String S3Url;
}
