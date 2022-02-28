package uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class MigrateCaseworkCaseRequest {

    private String type;

}
