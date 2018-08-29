package uk.gov.digital.ho.hocs.workflow.notifications;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final NotificationClient notifyClient;

    @Autowired
    public EmailService(@Value("${notify.apiKey}") String apiKey) {
        this.notifyClient = new NotificationClient(apiKey);
    }

    public void sendEmail(NotifyType notifyType, String emailAddress, Map<String, String> personalisation) throws NotificationClientException {
        log.info("Received request to sendEmailRequest {}, template Name {}, template ID {}", emailAddress, notifyType, notifyType.getDisplayValue());
        //TODO auditService
        notifyClient.sendEmail(notifyType.getDisplayValue(), emailAddress, personalisation, null);
    }
}
