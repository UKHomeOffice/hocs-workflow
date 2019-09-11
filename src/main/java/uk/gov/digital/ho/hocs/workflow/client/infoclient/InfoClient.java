package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.UserDto;

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
    public InfoClient(RestHelper restHelper, @Value("${hocs.info-service}") String infoService) {
        this.restHelper = restHelper;
        this.serviceBaseURL = infoService;
    }

    @Cacheable(value = "InfoClientGetCaseTypeByShortCode", unless = "#result == null", key = "#shortCode")
    public CaseDataType getCaseTypeByShortCode(String shortCode) {
        CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode), CaseDataType.class);
        log.info("Got CaseDataType {} for Short code {}", caseDataType.getDisplayCode(), shortCode, value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS));
        return caseDataType;
    }

    @Cacheable(value = "InfoClientGetSchemasForCaseType", unless = "#result.size() == 0", key = "#caseType")
    public Set<SchemaDto> getSchemasForCaseType(String caseType) {
        Set<SchemaDto> response = restHelper.get(serviceBaseURL, String.format("/schema/caseType/%s", caseType), new ParameterizedTypeReference<Set<SchemaDto>>() {});
        log.info("Got {} schemas", response.size(), value(EVENT, INFO_CLIENT_GET_SCHEMAS_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetSchema", unless = "#result == null", key = "#type")
    public SchemaDto getSchema(String type) {
        SchemaDto response = restHelper.get(serviceBaseURL, String.format("/schema/%s", type), SchemaDto.class);
        log.info("Got Form {}", type, value(EVENT, INFO_CLIENT_GET_FORM_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTeams", unless = "#result.size() == 0")
    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    @Cacheable(value = "InfoClientGetTeam", unless = "#result == null", key = "#teamUUID")
    public TeamDto getTeam(UUID teamUUID) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/%s", teamUUID),  TeamDto.class);
        log.info("Got Team teamUUID {}", response.getUuid(), value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTeamForStageType", unless = "#result == null", key = "#stageType")
    public UUID getTeamForStageType(String stageType) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/team", stageType),  TeamDto.class);
        log.info("Got Team teamUUID {} for Stage {}", response.getUuid(), stageType, value(EVENT, INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS));
        return response.getUuid();
    }

    @Cacheable(value = "InfoClientGetTeamForTopicAndStage", unless = "#result == null", key = "{ #caseUUID, #topicUUID, #stageType}")
    public TeamDto getTeamForTopicAndStage(UUID caseUUID, UUID topicUUID, String stageType) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/case/%s/topic/%s/stage/%s", caseUUID, topicUUID, stageType),  TeamDto.class);
        log.info("Got Team teamUUID {} for Topic {} and Stage {}", response.getUuid(), topicUUID, stageType, value(EVENT, INFO_CLIENT_GET_TEAM_FOR_TOPIC_STAGE_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTeamForRegionAndStage", unless = "#result == null", key = "{ #caseUUID, #regionUUID, #stageType}")
    public TeamDto getTeamForRegionAndStage(UUID caseUUID, UUID regionUUID, String stageType) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/case/%s/region/%s/stage/%s", caseUUID, regionUUID, stageType),  TeamDto.class);
        log.info("Got Team teamUUID {} for Case{} and Region {} and Stage {}", response.getUuid(), caseUUID, regionUUID, stageType, value(EVENT, INFO_CLIENT_GET_TEAM_FOR_REGION_STAGE_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTeamForConstituencyAndStage", unless = "#result == null", key = "{ #caseUUID, #constituencyUUID, #stageType}")
    public TeamDto getTeamForConstituencyAndStage(UUID caseUUID, UUID constituencyUUID, String stageType) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/case/%s/constituency/%s/stage/%s", caseUUID, constituencyUUID, stageType),  TeamDto.class);
        log.info("Got Team teamUUID {} for Constituency {} and Stage {}", response.getUuid(), constituencyUUID, stageType, value(EVENT, INFO_CLIENT_GET_TEAM_FOR_CONSTITUENCY_STAGE_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetUser", unless = "#result == null", key = "{ #userUUID}")
    public UserDto getUser(UUID userUUID) {
        UserDto userDto = restHelper.get(serviceBaseURL, String.format("/user/%s", userUUID), UserDto.class);
        log.info("Got User for UUID {}", userUUID, value(EVENT, INFO_CLIENT_GET_USER_SUCESS));
        return userDto;
    }
}