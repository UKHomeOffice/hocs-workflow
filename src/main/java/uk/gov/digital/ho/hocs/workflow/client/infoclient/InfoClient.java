package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.domain.model.StageType;

import java.time.LocalDate;
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

    public Deadline getCaseDeadline(CaseDataType caseType, LocalDate localDate) {
        ResponseEntity<Deadline> response = restHelper.get(serviceBaseURL, String.format("/casetype/%s/deadline/%s", caseType, localDate), Deadline.class);
        return response.getBody();
    }

    public Deadline getDeadline(StageType stageType, LocalDate localDate) {
        ResponseEntity<Deadline> response = restHelper.get(serviceBaseURL, String.format("/stagetype/%s/deadline/%s", stageType, localDate), Deadline.class);
        return response.getBody();
    }

    public UUID getTeam(String stageType) {
        // TODO:
        return UUID.fromString("44444444-2222-2222-2222-222222222222");
        //ResponseEntity<UUID> response = restHelper.get(serviceBaseURL, String.format("/stagetype/%s/team", stageType), UUID.class);
        //return response.getBody();
    }


}