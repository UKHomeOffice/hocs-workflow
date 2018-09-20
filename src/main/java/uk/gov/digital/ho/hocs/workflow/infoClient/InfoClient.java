package uk.gov.digital.ho.hocs.workflow.infoClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.dto.GetParentTopicResponse;
import uk.gov.digital.ho.hocs.workflow.model.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InfoClient {

    private final RestHelper restHelper;
    private final String serviceBaseURL;


    @Autowired
    public InfoClient(RestHelper restHelper,
                      @Value("${hocs.info-service}") String infoService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = infoService;
    }

   public Map<StageType, LocalDate> getDeadlines(CaseType caseType, LocalDate localDate) {
       ResponseEntity<InfoGetDeadlinesResponse> response = restHelper.get(serviceBaseURL, String.format("/casetype/%s/deadlines/%s", caseType, localDate), InfoGetDeadlinesResponse.class);
       Map<StageType, LocalDate> deadlines = response.getBody().getDeadlines();
       return deadlines;
   }

   public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
       ResponseEntity<InfoGetNominatedPeopleResponse> response = restHelper.get(serviceBaseURL, String.format("/nominatedpeople/%s", teamUUID), InfoGetNominatedPeopleResponse.class);
       Set<InfoNominatedPeople> nominatedPeople = response.getBody().getNominatedPeople();
       return nominatedPeople;
   }
  
    public InfoGetTemplateResponse getTemplate(CaseType caseType) {
        ResponseEntity<InfoGetTemplateResponse> response = restHelper.get(serviceBaseURL, String.format("/casetype/%s/template/%s", caseType, templateUUID), InfoGetTemplateResponse.class);
        InfoGetTemplateResponse template = response.getBody();
        return template;
    }

    public InfoGetStandardLineResponse getStandardLine(CaseType caseType, UUID standardLineUUID) {
        ResponseEntity<InfoGetStandardLineResponse> response = getWithAuth(String.format("/casetype/%s/standardlinekey/%s", caseType, standardLineUUID), null, InfoGetStandardLineResponse.class);
        InfoGetStandardLineResponse standardLineKey = response.getBody();
        return standardLineKey;
    }

    public GetParentTopicResponse getParentTopics(String caseType) {
        ResponseEntity<GetParentTopicResponse> response = restHelper.get(serviceBaseURL, String.format("/topic/parent/%s", caseType), GetParentTopicResponse.class);
        GetParentTopicResponse topics = response.getBody();
        return topics;
    }

}