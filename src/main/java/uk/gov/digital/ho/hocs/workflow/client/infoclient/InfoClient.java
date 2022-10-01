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
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.CaseDetailsFieldDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.UnitDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.UserDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_CASE_DETAILS_FIELDS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_SCHEMAS_SUCCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_TEAMS_SUCCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_TEAM_FOR_TOPIC_STAGE_SUCCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_TEAM_SUCCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_UNIT_FOR_TEAM_SUCESS;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.INFO_CLIENT_GET_USER_SUCESS;

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
        CaseDataType caseDataType = restHelper.get(serviceBaseURL, String.format("/caseType/shortCode/%s", shortCode),
            CaseDataType.class);
        log.info("Got CaseDataType {} for Short code {}", caseDataType.getDisplayCode(), shortCode,
            value(EVENT, INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS));
        return caseDataType;
    }

    @Deprecated(forRemoval = true)
    @Cacheable(value = "InfoClientGetSchemasForCaseTypeAndStages", unless = "#result.size() == 0")
    public List<SchemaDto> getSchemasForCaseTypeAndStages(String caseType, String caseStages) {
        List<SchemaDto> response = restHelper.get(serviceBaseURL,
            String.format("/schema/caseType/%s?stages=%s", caseType, caseStages),
            new ParameterizedTypeReference<List<SchemaDto>>() {});
        log.info("Got {} schemas", response.size(), value(EVENT, INFO_CLIENT_GET_SCHEMAS_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTeams", unless = "#result.size() == 0")
    public Set<TeamDto> getTeams() {
        Set<TeamDto> teams = restHelper.get(serviceBaseURL, "/team", new ParameterizedTypeReference<Set<TeamDto>>() {});
        log.info("Got {} teams", teams.size(), value(EVENT, INFO_CLIENT_GET_TEAMS_SUCCESS));
        return teams;
    }

    @Cacheable(value = "InfoClientGetAllStageTypes", unless = "#result.size() == 0")
    public Set<StageTypeDto> getAllStageTypes() {
        Set<StageTypeDto> response = restHelper.get(serviceBaseURL, "/stageType",
            new ParameterizedTypeReference<Set<StageTypeDto>>() {});
        log.info("Got {} Stage Types", response.size(), value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTeam", unless = "#result == null", key = "#teamUUID")
    public TeamDto getTeam(UUID teamUUID) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/team/%s", teamUUID), TeamDto.class);
        log.info("Got Team teamUUID {}", response.getUuid(), value(EVENT, INFO_CLIENT_GET_TEAM_SUCCESS));
        return response;
    }

    @Cacheable(value = "InfoClientGetTeamForStageType", unless = "#result == null", key = "#stageType")
    public UUID getTeamForStageType(String stageType) {
        TeamDto response = restHelper.get(serviceBaseURL, String.format("/stageType/%s/team", stageType),
            TeamDto.class);
        log.info("Got Team teamUUID {} for Stage {}", response.getUuid(), stageType,
            value(EVENT, INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS));
        return response.getUuid();
    }

    @Cacheable(value = "InfoClientGetTeamForTopicAndStage",
               unless = "#result == null",
               key = "{ #caseUUID, #topicUUID, #stageType}")
    public TeamDto getTeamForTopicAndStage(UUID caseUUID, UUID topicUUID, String stageType) {
        TeamDto response = restHelper.get(serviceBaseURL,
            String.format("/team/case/%s/topic/%s/stage/%s", caseUUID, topicUUID, stageType), TeamDto.class);
        log.info("Got Team teamUUID {} for Topic {} and Stage {}", response.getUuid(), topicUUID, stageType,
            value(EVENT, INFO_CLIENT_GET_TEAM_FOR_TOPIC_STAGE_SUCCESS));
        return response;
    }

    @Deprecated(forRemoval = true)
    @Cacheable(value = "InfoClientGetUser", unless = "#result == null", key = "{ #userUUID}")
    public UserDto getUser(UUID userUUID) {
        UserDto userDto = restHelper.get(serviceBaseURL, String.format("/user/%s", userUUID), UserDto.class);
        log.info("Got User for UUID {}", userUUID, value(EVENT, INFO_CLIENT_GET_USER_SUCESS));
        return userDto;
    }

    @Cacheable(value = "InfoClientGetUnitForTeam", unless = "#result == null", key = "{ #teamUUID }")
    public UnitDto getUnitForTeam(UUID teamUUID) {
        UnitDto unitDto = restHelper.get(serviceBaseURL, String.format("/unit/team/%s", teamUUID), UnitDto.class);
        log.info("Got Unit {} for Team {}", teamUUID, value(EVENT, INFO_CLIENT_GET_UNIT_FOR_TEAM_SUCESS));
        return unitDto;
    }

    @Deprecated(forRemoval = true)
    @Cacheable(value = "InfoClientGetCaseDetailsFieldDtos", unless = "#result == null", key = "{ #caseType}")
    public List<CaseDetailsFieldDto> getCaseDetailsFieldsByCaseType(String caseType) {
        List<CaseDetailsFieldDto> caseDetailsFieldDtos = restHelper.get(serviceBaseURL,
            String.format("/caseDetailsFields/%s", caseType),
            new ParameterizedTypeReference<List<CaseDetailsFieldDto>>() {});
        log.info("Got CaseDetailsFields By Case Type {} ", value(EVENT, INFO_CLIENT_GET_CASE_DETAILS_FIELDS));
        return caseDetailsFieldDtos;
    }

}
