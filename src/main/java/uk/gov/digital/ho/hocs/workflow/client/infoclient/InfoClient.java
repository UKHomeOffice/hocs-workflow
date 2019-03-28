package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.*;

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

    @Cacheable(value = "InfoClientGetTeamForStageType")
    public UUID getTeamForStageType(String stageType) {
        ResponseEntity<TeamDto> response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/team", stageType),  TeamDto.class);
        return response.getBody().getUuid();
    }

    @Cacheable(value = "InfoClientGetTeamForTopicAndStage")
    public TeamDto getTeamForTopicAndStage(UUID caseUUID, UUID topicUUID, String stageType) {
        ResponseEntity<TeamDto> response = restHelper.get(serviceBaseURL, String.format("/team/case/%s/topic/%s/stage/%s", caseUUID, topicUUID, stageType),  TeamDto.class);
        return response.getBody();
    }

    @Cacheable(value = "InfoClientGetTeam")
    public TeamDto getTeam(UUID teamUUID) {
        ResponseEntity<TeamDto> response = restHelper.get(serviceBaseURL, String.format("/team/%s", teamUUID),  TeamDto.class);
        return response.getBody();
    }

    @Cacheable(value = "InfoClientGetTeams")
    public Set<TeamDto> getTeams() {
        try {
            ResponseEntity<Set<TeamDto>> response = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
            log.info("Got teams {}", response.getBody().size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
            return response.getBody();
        } catch (Exception e) {
            log.error("Could not get teams", value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
            throw new ApplicationExceptions.EntityNotFoundException("Could not get teams", INFO_CLIENT_GET_TEAMS_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetForm")
    public SchemaDto getForm(String type) {
        ResponseEntity<SchemaDto> response = restHelper.get(serviceBaseURL, String.format("/schema/%s", type), SchemaDto.class);
        return response.getBody();
    }
}