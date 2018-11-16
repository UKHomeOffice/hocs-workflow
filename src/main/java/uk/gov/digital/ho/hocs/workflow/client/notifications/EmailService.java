package uk.gov.digital.ho.hocs.workflow.client.notifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.client.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoClient.InfoNominatedPeople;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class EmailService {

    private final NotificationClient notifyClient;
    private final InfoClient infoClient;
    private final String URL;

    @Autowired
    public EmailService(InfoClient infoClient,
                        @Value("${notify.apiKey}") String apiKey,
                        @Value("${hocs.url}") String URL) {
        this.notifyClient = new NotificationClient(apiKey);
        this.infoClient = infoClient;
        this.URL = URL;
    }

    public void sendEmail(String caseUUIDString, String caseRef, String stageUUIDString, String teamUUIDString, NotifyType notifyType) {
        String link = URL + "/case/" + caseUUIDString + "/stage/" + stageUUIDString;
        log.info("Generating email personalisation");
        Set<InfoNominatedPeople> nominatedPeopleSet = infoClient.getNominatedPeople(UUID.fromString(teamUUIDString));
        log.info("Nominated people set size - " + nominatedPeopleSet.size());
        for (InfoNominatedPeople nominatedPeople : nominatedPeopleSet) {
            Map<String, String> personalisation = new HashMap<>();
            personalisation.put("link", link);
            personalisation.put("caseRef", caseRef);
            sendEmail(notifyType, nominatedPeople.getEmailAddress(), personalisation);
        }
    }

    private void sendEmail(NotifyType notifyType, String emailAddress, Map<String, String> personalisation) {
        log.info("Received request to sendEmailRequest {}, template Name {}, template ID {}", emailAddress, notifyType, notifyType.getDisplayValue());
        //TODO auditService

        try {
            notifyClient.sendEmail(notifyType.getDisplayValue(), emailAddress, personalisation, null);
        } catch (NotificationClientException e) {
            log.error(e.getLocalizedMessage());
            log.warn("Didn't send email to {}", emailAddress);
        }

    }
}
