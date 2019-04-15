package uk.gov.digital.ho.hocs.workflow.client.infoclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.StageType;
import uk.gov.digital.ho.hocs.workflow.application.RestHelper;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;

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

    @Cacheable(value = "InfoClientGetCaseTypes", unless = "#result.size() == 0")
    public Set<CaseDataType> getCaseTypes() {
        Set<CaseDataType> response = restHelper.get(serviceBaseURL, "/caseType", new ParameterizedTypeReference<Set<CaseDataType>>() {});
        log.info("Got {} case types", response.size(), value(EVENT, INFO_CLIENT_GET_CASE_TYPES_SUCCESS));
        return response;
    }

    @CachePut(value = "InfoClientGetCaseTypeByShortCode", unless = "#result == null", key = "#shortCode")
    public CaseDataType populateCaseTypeByShortCode(String shortCode, CaseDataType caseDataType) {
        return caseDataType;
    }

    @Cacheable(value = "InfoClientGetCaseTypeByShortCode", unless = "#result == null", key = "#shortCode")
    public CaseDataType getCaseTypeByShortCode(String shortCode) {
        CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode), CaseDataType.class);
        log.info("Got CaseDataType {} for Short code {}", caseDataType.getDisplayCode(), shortCode, value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS));
        return caseDataType;
    }

    @CachePut(value = "InfoClientGetSchemasForCaseType", unless = "#result.size() == 0", key = "#caseType")
    public Set<SchemaDto> populateSchemasForCaseType(String caseType) {
        return getSchemasForCaseType(caseType);
    }

    @Cacheable(value = "InfoClientGetSchemasForCaseType", unless = "#result.size() == 0", key = "#caseType")
    public Set<SchemaDto> getSchemasForCaseType(String caseType) {
        Set<SchemaDto> response = restHelper.get(serviceBaseURL, String.format("/schema/caseType/%s", caseType), new ParameterizedTypeReference<Set<SchemaDto>>() {});
        log.info("Got {} schemas", response.size(), value(EVENT, INFO_CLIENT_GET_SCHEMAS_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetSchema", unless = "#result == null", key = "#type")
    public SchemaDto populateSchema(String type, SchemaDto schemaDto) {
        return schemaDto;
    }

    @Cacheable(value = "InfoClientGetSchema", unless = "#result == null", key = "#type")
    public SchemaDto getSchema(String type) {
        SchemaDto response = restHelper.get(serviceBaseURL, String.format("/schema/%s", type), SchemaDto.class);
        log.info("Got Form {}", type, value(EVENT, INFO_CLIENT_GET_FORM_SUCCESS));
        return response;
    }

    @CachePut(value = "InfoClientGetTeams", unless = "#result.size() == 0")
    public Set<TeamDto> populateTeams() {
        return getTeams();
    }

    @Cacheable(value = "InfoClientGetTeams", unless = "#result.size() == 0")
    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    @CachePut(value = "InfoClientGetTeam", unless = "#result == null", key = "#teamUUID")
    public TeamDto populateTeam(UUID teamUUID, TeamDto teamDto) {
        return teamDto;
    }

    @Cacheable(value = "InfoClientGetTeam", unless = "#result == null", key = "#teamUUID")
    public TeamDto getTeam(UUID teamUUID) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/%s", teamUUID),  TeamDto.class);
        log.info("Got Team teamUUID {}", response.getUuid(), value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
        return response;
    }

    public Set<StageType> getAllStageTypes() {
        Set<StageType> response = restHelper.get(serviceBaseURL, "/stageType",  new ParameterizedTypeReference<Set<StageType>>() {});
        log.info("Got {} StageTypes", response.size(), value(EVENT, INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS));
        return response;
    }

    @CachePut(value = "InfoClientGetTeamForStageType", unless = "#result == null", key = "#stageType")
    public UUID populateTeamForStageType(String stageType) {
        return getTeamForStageType(stageType);
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
}