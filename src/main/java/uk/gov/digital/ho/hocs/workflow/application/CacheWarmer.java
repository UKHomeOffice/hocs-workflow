package uk.gov.digital.ho.hocs.workflow.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.workflow.api.dto.SchemaDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.StageType;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;

import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.CACHE_PRIME_FAILED;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.EVENT;

@Slf4j
@Component
@Profile({"cache"})
public class CacheWarmer {

    private InfoClient infoClient;

    @Autowired
    public CacheWarmer(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        primeCaches();
    }

    private void primeCaches() {
        primeCaseTypes();
        primeTeamCache();
        primeStageType();
    }

    @Scheduled(cron = "0 0/30 6-18 * * MON-FRI")
    private void primeCaseTypes(){
        try {
            Set<CaseDataType> caseDataTypeSet = this.infoClient.getCaseTypes();
            for(CaseDataType caseDataType : caseDataTypeSet) {
                this.infoClient.populateCaseTypeByShortCode(caseDataType.getShortCode(), caseDataType);
                Set<SchemaDto> schemas = this.infoClient.populateSchemasForCaseType(caseDataType.getDisplayCode());
                for(SchemaDto schemaDto : schemas) {
                    this.infoClient.populateSchema(schemaDto.getType(), schemaDto);
                }
            }
        } catch(Exception e) {
            log.warn("Failed to prime Case Types. : {}",e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "5 0/30 6-18 * * MON-FRI")
    private void primeTeamCache(){
        try {
            Set<TeamDto> teams = this.infoClient.populateTeams();
            for(TeamDto teamDto : teams) {
                this.infoClient.populateTeam(teamDto.getUuid(), teamDto);
            }
        } catch(Exception e) {
            log.warn("Failed to prime Teams. : {}", e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "10 0/30 6-18 * * MON-FRI")
    private void primeStageType(){
        try {
            Set<StageType> stageTypes = this.infoClient.getAllStageTypes();
            for(StageType stageType : stageTypes) {
                this.infoClient.populateTeamForStageType(stageType.getDisplayCode());
            }
        } catch(Exception e) {
            log.warn("Failed to prime Teams. : {}", e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

}