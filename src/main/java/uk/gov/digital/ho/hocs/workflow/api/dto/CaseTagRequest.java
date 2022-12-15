package uk.gov.digital.ho.hocs.workflow.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CaseTagRequest {
    @JsonProperty("tag")
    private String tag;

    public CaseTagRequest() {
    }

    public CaseTagRequest(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
