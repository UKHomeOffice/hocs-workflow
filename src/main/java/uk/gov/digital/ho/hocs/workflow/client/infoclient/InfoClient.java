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

    public LocalDate getCaseDeadline(CaseDataType caseType, LocalDate localDate) {
        ResponseEntity<LocalDate> response = restHelper.get(serviceBaseURL, String.format("/caseType/%s/deadline?received=%s", caseType, localDate), LocalDate.class);
        return response.getBody();
    }

    public Deadline getDeadline(StageType stageType, LocalDate localDate) {
        ResponseEntity<Deadline> response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/deadline?received=%s", stageType, localDate), Deadline.class);
        return response.getBody();
    }

    public UUID getTeam(String stageType) {
        ResponseEntity<TeamDto> response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/team", stageType),  TeamDto.class);
        return response.getBody().getUuid();
    }


}