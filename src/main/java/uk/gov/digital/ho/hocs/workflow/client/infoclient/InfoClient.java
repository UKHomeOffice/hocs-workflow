package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;

import java.util.Optional;
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
        try {
            TeamDto response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/team", stageType), TeamDto.class);
            log.info("Got Team for StageType {}", stageType, value(EVENT, INFO_CLIENT_GET_TEAM_BY_STAGE_SUCCESS));
            return response.getUuid();
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Team for StageType %s. %s", stageType, e.toString()), INFO_CLIENT_GET_TEAM_BY_STAGE_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetTeamForTopicAndStage")
    public TeamDto getTeamForTopicAndStage(UUID caseUUID, UUID topicUUID, String stageType) {
        try {
            TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/case/%s/topic/%s/stage/%s", caseUUID, topicUUID, stageType), TeamDto.class);
            log.info("Got Team for StageType {} and Topic {}", stageType, topicUUID, value(EVENT, INFO_CLIENT_GET_TEAM_BY_TOPIC_STAGE_SUCCESS));
            return response;
        } catch (ApplicationExceptions.ResourceException e) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Team for StageType %s and Topic %s. %s", stageType, topicUUID, e.toString()), INFO_CLIENT_GET_TEAM_BY_TOPIC_STAGE_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetTeam")
    public TeamDto getTeam(UUID teamUUID) {
        Optional<TeamDto> teamDto = getTeamFromTeams(teamUUID);
        if(teamDto.isPresent()) {
            return teamDto.get();
        } else {
            try {
                TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/%s", teamUUID), TeamDto.class);
                log.info("Got Team {}", teamUUID, value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
                return response;
            } catch (ApplicationExceptions.ResourceException e) {
                throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Team %s. %s", teamUUID, e.toString()), INFO_CLIENT_GET_TEAM_FAILURE);
            }
        }
    }

    @Cacheable(value = "InfoClientGetTeams")
    public Set<TeamDto> getTeams() {
        try {
            Set<TeamDto> response = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
            log.info("Got teams {}", response.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
            return response;
        } catch (Exception e) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get teams. %s", e.toString()), INFO_CLIENT_GET_TEAMS_FAILURE);
        }
    }

    @Cacheable(value = "InfoClientGetSchema")
    public SchemaDto getSchema(String type) {
        try {
            SchemaDto response = restHelper.get(serviceBaseURL, String.format("/schema/%s", type), SchemaDto.class);
            log.info("Got Schema {}", type, value(EVENT, INFO_CLIENT_GET_SCHEMA_SUCCESS));
            return response;
        } catch (Exception e) {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Could not get Schema. %s", e.toString()), INFO_CLIENT_GET_SCHEMA_FAILURE);

        }
    }

    private Optional<TeamDto> getTeamFromTeams(UUID teamUUID) {
        return getTeams().stream().filter(teamDto -> teamDto.getUuid().equals(teamUUID)).findFirst();
    }
}