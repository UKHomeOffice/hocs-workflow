package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.api.dto.Topic;
import uk.gov.digital.ho.hocs.workflow.domain.model.StageType;

import java.time.LocalDate;
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

    public Deadline getDeadline(StageType stageType, LocalDate localDate) {
        ResponseEntity<Deadline> response = restHelper.get(serviceBaseURL, String.format("/stagetype/%s/deadline/%s", stageType, localDate), Deadline.class);
        Deadline deadlines = response.getBody();
        return deadlines;
    }

    public Set<InfoNominatedPeople> getNominatedPeople(UUID teamUUID) {
        ResponseEntity<InfoGetNominatedPeopleResponse> response = restHelper.get(serviceBaseURL, String.format("/nominatedpeople/%s", teamUUID), InfoGetNominatedPeopleResponse.class);
        Set<InfoNominatedPeople> nominatedPeople = response.getBody().getNominatedPeople();
        return nominatedPeople;
    }

    public Topic getTopic(UUID topicUUID) {
        ResponseEntity<Topic> response = restHelper.get(serviceBaseURL, String.format("/topic/%s", topicUUID), Topic.class);
        Topic topic = response.getBody();
        return topic;
    }

}