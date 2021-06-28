package uk.gov.digital.ho.hocs.migration.client.caseworkclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.migration.Case;
import uk.gov.digital.ho.hocs.migration.Stage;
import uk.gov.digital.ho.hocs.migration.UpdateActiveStageTeamRequest;

@Slf4j
@Service
public class CaseworkClient {
    public static final String WCS_CASEWORK_TEAM_1 = "9boxzWRzScmn7xNQgjeV7w";
    private final RestTemplate restTemplate;
    private final String serviceBaseURL;

    public CaseworkClient(RestTemplate restTemplate, @Value("${hocs.case-service}") String caseworkUrl){
        this.restTemplate = restTemplate;
        this.serviceBaseURL = caseworkUrl;
    }

    public Case getCase(String caseUuid) throws JsonProcessingException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Auth-Groups", WCS_CASEWORK_TEAM_1);
        HttpEntity request = new HttpEntity(httpHeaders);

        ResponseEntity response =
                restTemplate.exchange(serviceBaseURL +"/case/" + caseUuid , HttpMethod.GET,
                request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.getBody().toString(), Case.class);
    }

    public Stage getActiveStage (String caseUuid){
        Stage activeStage =
                restTemplate.getForObject(serviceBaseURL +"/case/" + caseUuid +"/activeStage",
                        Stage.class);

        log.info("Active stage found: {}", activeStage.toString());
        return activeStage;
    }

    public void updateStageTeam(String caseUuid, String stageUuid, String newTeamUuid){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Auth-Groups", WCS_CASEWORK_TEAM_1);

        UpdateActiveStageTeamRequest requestBody = new UpdateActiveStageTeamRequest(newTeamUuid);
        HttpEntity request = new HttpEntity(requestBody, httpHeaders);

        restTemplate.put(serviceBaseURL +"/case/" + caseUuid + "/stage/" + stageUuid + "/team",
                request);

        log.info("Updated stage team: {}", newTeamUuid);
    }
}
