package uk.gov.digital.ho.hocs.workflow.notifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.infoClient.InfoNominatedPeople;
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

    public void buildEmailToSend(String caseUUIDString, String caseRef, String stageUUIDString, String teamUUIDString, NotifyType notifyType) throws NotificationClientException {
        String link = URL + "case/" + caseUUIDString + "/stage/" + stageUUIDString;

        Set<InfoNominatedPeople> nominatedPeopleSet = infoClient.getNominatedPeople(UUID.fromString(teamUUIDString));

        for (InfoNominatedPeople nominatedPeople : nominatedPeopleSet) {
            Map<String, String> personalisation = new HashMap<>();
            personalisation.put("link", link);
            personalisation.put("caseRef", caseRef);
            sendEmail(notifyType, nominatedPeople.getEmailAddress(), personalisation);
        }
    }

    public void sendEmail(NotifyType notifyType, String emailAddress, Map<String, String> personalisation) throws NotificationClientException {
        log.info("Received request to sendEmailRequest {}, template Name {}, template ID {}", emailAddress, notifyType, notifyType.getDisplayValue());
        //TODO auditService
        notifyClient.sendEmail(notifyType.getDisplayValue(), emailAddress, personalisation, null);
    }
}
