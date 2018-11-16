package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.api.dto.GetParentTopicResponse;
import uk.gov.digital.ho.hocs.workflow.api.dto.Topic;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.domain.model.StageType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

   public Map<StageType, LocalDate> getDeadlines(CaseDataType caseDataType, LocalDate localDate) {
       ResponseEntity<InfoGetDeadlinesResponse> response = restHelper.get(serviceBaseURL, String.format("/casetype/%s/deadlines/%s", caseDataType, localDate), InfoGetDeadlinesResponse.class);
       Map<StageType, LocalDate> deadlines = response.getBody().getDeadlines();
       return deadlines;
   }

   public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
       ResponseEntity<InfoGetNominatedPeopleResponse> response = restHelper.get(serviceBaseURL, String.format("/nominatedpeople/%s", teamUUID), InfoGetNominatedPeopleResponse.class);
       Set<InfoNominatedPeople> nominatedPeople = response.getBody().getNominatedPeople();
       return nominatedPeople;
   }

    public GetParentTopicResponse getParentTopics(String caseType) {
        ResponseEntity<GetParentTopicResponse> response = restHelper.get(serviceBaseURL, String.format("/topic/parent/%s", caseType), GetParentTopicResponse.class);
        GetParentTopicResponse topics = response.getBody();
        return topics;
    }

    public GetParentTopicResponse getParentTopicsAndTopics(String caseType) {
        ResponseEntity<GetParentTopicResponse> response = restHelper.get(serviceBaseURL, String.format("/topics/%s", caseType), GetParentTopicResponse.class);
        GetParentTopicResponse topics = response.getBody();
        return topics;
    }

    public Topic getTopic(UUID topicUUID) {
        ResponseEntity<Topic> response = restHelper.get(serviceBaseURL, String.format("/topic/%s", topicUUID), Topic.class);
        Topic topic = response.getBody();
        return topic;
    }

    public InfoGetTemplateResponse getTemplate(CaseDataType caseDataType) {
        ResponseEntity<InfoGetTemplateResponse> response = restHelper.get(serviceBaseURL, String.format("/templates/%s", caseDataType), InfoGetTemplateResponse.class);
        InfoGetTemplateResponse template = response.getBody();
        return template;
    }

    public InfoGetStandardLineResponse getStandardLine( UUID topicUUID) {
        ResponseEntity<InfoGetStandardLineResponse> response = restHelper.get(serviceBaseURL, String.format("/standardlines/%s", topicUUID), InfoGetStandardLineResponse.class);
        InfoGetStandardLineResponse template = response.getBody();
        return template;
    }

}