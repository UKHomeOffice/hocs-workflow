package uk.gov.digital.ho.hocs.workflow;

import java.text.SimpleDateFormat;
import java.time.Clock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.workflow.client.camundaclient.CamundaClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.CaseworkClient;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCaseworkCaseDataResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCorrespondentResponse;
import uk.gov.digital.ho.hocs.workflow.client.caseworkclient.dto.GetCorrespondentsResponse;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.UserDto;
import uk.gov.digital.ho.hocs.workflow.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.workflow.util.NoteType;
import uk.gov.digital.ho.hocs.workflow.util.NumberUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.LocalTime.now;

@Service
@Slf4j
public class BpmnService {

    private final CaseworkClient caseworkClient;
    private final CamundaClient camundaClient;
    private final InfoClient infoClient;
    private final Clock clock;


    @Autowired
    public BpmnService(CaseworkClient caseworkClient,
                       CamundaClient camundaClient,
                       InfoClient infoClient,
                       Clock clock) {
        this.caseworkClient = caseworkClient;
        this.camundaClient = camundaClient;
        this.infoClient = infoClient;
        this.clock = clock;
    }

    public String createStage(String caseUUIDString, String stageUUIDString, String stageTypeString, String allocationType, String allocationTeamString) {
        return createStage(caseUUIDString, stageUUIDString, stageTypeString, allocationType, allocationTeamString, null);
    }

    public String createStage(String caseUUIDString, String stageUUIDString, String stageTypeString, String allocationType, String allocationTeamString, String allocatedUserId) {
        log.debug("Creating or Updating Stage {} for case {}", stageTypeString, caseUUIDString);

        UUID teamUUID = deriveTeamUUID(caseUUIDString, stageTypeString, allocationTeamString);
        UUID userUUID = deriveUserUUID(caseUUIDString, stageTypeString, allocatedUserId);
        if (teamUUID != null && userUUID != null) {
            UserDto userInTeam = infoClient.getUserForTeam(teamUUID, userUUID);
            if (userInTeam == null) {
                log.info("Requested user {} for new stage {} is not in team {}", userUUID, stageTypeString, teamUUID);
                userUUID = null;
            }
        }

        String resultStageUUID;
        if (StringUtils.hasText(stageUUIDString)) {
            log.debug("Stage {} already exists for case {}, recreating stage {}", stageTypeString, caseUUIDString, stageUUIDString);
            recreateStage(caseUUIDString, stageUUIDString, stageTypeString, allocationType, teamUUID, userUUID);
            resultStageUUID = stageUUIDString;
            log.info("Updated Stage {} for Case {}", stageUUIDString, caseUUIDString);
        } else {
            log.debug("Creating new stage {} for case {}, assigning to team {}", stageTypeString, caseUUIDString, teamUUID);
            resultStageUUID = caseworkClient.createStage(UUID.fromString(caseUUIDString), stageTypeString, teamUUID, userUUID, allocationType).toString();
            log.info("Created Stage {} for Case {}", resultStageUUID, caseUUIDString);
        }

        return resultStageUUID;
    }

    public void completeStage(String caseUUIDString, String stageUUIDString) {
        caseworkClient.updateStageTeam(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), null, null);
        log.info("Completed Stage {} for Case {}", stageUUIDString, caseUUIDString);
    }

    public void completeCase(String caseUUIDString) {
        caseworkClient.completeCase(UUID.fromString(caseUUIDString), true);
        log.info("Completed Case {}", caseUUIDString);
    }

    public void calculateTotals(String caseUUIDString, String stageUUIDString, String listName) {
        caseworkClient.calculateTotals(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), listName);
        log.info("Calculated totals WCS for Case {}", caseUUIDString);
    }

    public void updateDeadline(String caseUUIDString, String stageUUIDString, String dateReceived) {
        caseworkClient.updateDateReceived(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), LocalDate.parse(dateReceived));
    }

    public void updateDispatchDeadlineDate(String caseUUIDString, String stageUUIDString, String dispatchDeadlineDate) {
        caseworkClient.updateDispatchDeadlineDate(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), LocalDate.parse(dispatchDeadlineDate));
    }

    public void updateDeadlineDays(String caseUUIDString, String stageUUIDString, String daysString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        int days = Integer.parseInt(daysString);
        caseworkClient.updateDeadlineDays(caseUUID, stageUUID, days);
    }

    public void updateStageDeadline(String caseUUIDString, String stageUUIDString, String stageType, String daysString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        int days = Integer.parseInt(daysString);
        caseworkClient.updateStageDeadline(caseUUID, stageUUID, stageType, days);
    }

    public void updateDeadlineForStages(String caseUUIDString, String stageUUIDString, String... stageTypeAndDaysPair) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> stageTypeAndDaysMap = parseArgPairs(stageTypeAndDaysPair);

        Map<String, Integer> convertedStageTypeAndDaysMap = stageTypeAndDaysMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, entry -> Integer.parseInt(entry.getValue())
                )
        );
        caseworkClient.updateDeadlineForStages(caseUUID, stageUUID, convertedStageTypeAndDaysMap);
    }

    public void updatePrimaryCorrespondent(String caseUUIDString, String stageUUIDString, String correspondentUUIDString) {
        caseworkClient.updatePrimaryCorrespondent(UUID.fromString(caseUUIDString), UUID.fromString(stageUUIDString), UUID.fromString(correspondentUUIDString));
        log.info("Updated Primary Correspondent for Case {}", caseUUIDString);
    }

    public boolean caseHasMember(String caseUUIDString) {
        log.info("Checking Correspondents for Case {}", caseUUIDString);
        boolean memberPresent = false;

        GetCorrespondentsResponse correspondents = caseworkClient.getCorrespondentsForCase(UUID.fromString(caseUUIDString));

        if (correspondents != null) {
            // collect any members in the correspondents list
            List<GetCorrespondentResponse> members = correspondents.getCorrespondents().stream().filter(correspondent ->
                    correspondent.getType().equals("MEMBER")).collect(Collectors.toList());
            members.forEach(member -> {
                log.info("Member : " + member);
            });
            memberPresent = (members.size() > 0);
        }

        log.info("Members present ? : " + memberPresent);
        return memberPresent;
    }

    public boolean caseHasPrimaryCorrespondentType(String caseUUIDString, String type) {
        log.debug("Checking Case {} has primary correspondent type {}", caseUUIDString, type);
        UUID caseUUID = UUID.fromString(caseUUIDString);
        boolean validPrimaryCorrespondent = false;

        GetCaseworkCaseDataResponse caseData = caseworkClient.getCase(caseUUID);
        GetCorrespondentsResponse correspondents = caseworkClient.getCorrespondentsForCase(caseUUID);

        if (caseData != null && correspondents != null) {
            for(GetCorrespondentResponse correspondent : correspondents.getCorrespondents()) {
                if (correspondent.getUuid().equals(caseData.getPrimaryCorrespondentUUID())) {
                    if (correspondent.getType().equals(type)){
                        validPrimaryCorrespondent = true;
                    }
                    break;
                }
            }
        }

        log.info("Case {} has primary correspondent type {} : {}", caseUUIDString, type, validPrimaryCorrespondent);
        return validPrimaryCorrespondent;
    }

    public void updatePrimaryTopic(String caseUUIDString, String stageUUIDString, String topicUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        UUID topicUUID = UUID.fromString(topicUUIDString);

        caseworkClient.updatePrimaryTopic(caseUUID, stageUUID, topicUUID);
    }

    public void updateTeamsForPrimaryTopic(String caseUUIDString, String stageUUIDString, String topicUUIDString, String stageType, String teamNameKey, String teamUUIDKey) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        UUID topicUUID = UUID.fromString(topicUUIDString);

        Map<String, String> teamsForTopic = new HashMap<>();
        TeamDto teamDto = infoClient.getTeamForTopicAndStage(caseUUID, topicUUID, stageType);
        if (teamDto.isActive()) {
            teamsForTopic.put(teamNameKey, teamDto.getUuid().toString());
            teamsForTopic.put(teamUUIDKey, teamDto.getDisplayName());
        } else {
            log.warn("Avoiding assigning orphaned team {}", teamDto.getDisplayName());
        }
        camundaClient.updateTask(stageUUID, teamsForTopic);
        caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);

        log.debug("######## Updated Primary Topic ########");
    }

    public void setDraftingTeam(String caseUUIDString, String stageUUIDString, String draftingUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        Map<String, String> teamsForTopic = new HashMap<>();

        log.info("Writing draft team with {}", draftingUUIDString);
        TeamDto draftingTeam = infoClient.getTeam(UUID.fromString(draftingUUIDString));

        teamsForTopic.put("OverrideDraftingTeamUUID", "");
        teamsForTopic.put("OverrideDraftingTeamName", "");

        teamsForTopic.put("DraftingTeamUUID", draftingTeam.getUuid().toString());
        teamsForTopic.put("DraftingTeamName", draftingTeam.getDisplayName());

        camundaClient.updateTask(stageUUID, teamsForTopic);
        caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);
    }

    public void updateTeamSelection(String caseUUIDString, String stageUUIDString, String draftingUUIDString, String privateOfficeUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> teamsForTopic = new HashMap<>();

        if (StringUtils.hasText(draftingUUIDString)) {
            TeamDto draftingTeam = infoClient.getTeam(UUID.fromString(draftingUUIDString));

            if (draftingTeam.isActive()){
                teamsForTopic.put("OverrideDraftingTeamUUID", draftingTeam.getUuid().toString());
                teamsForTopic.put("OverrideDraftingTeamName", draftingTeam.getDisplayName());
                log.info("Overwriting draft team with {}", draftingUUIDString);
            }
            else {
                teamsForTopic.put("OverrideDraftingTeamUUID", "");
                teamsForTopic.put("OverrideDraftingTeamName", "");
                log.info("Removing Override Drafting Team as selected team was inactive.");
            }


        }

        if (StringUtils.hasText(privateOfficeUUIDString)) {
            TeamDto pOTeam = infoClient.getTeam(UUID.fromString(privateOfficeUUIDString));
            if (pOTeam.isActive()){
                teamsForTopic.put("OverridePOTeamUUID", pOTeam.getUuid().toString());
                teamsForTopic.put("OverridePOTeamName", pOTeam.getDisplayName());
                log.info("Overwriting po team with {}", privateOfficeUUIDString);
            }
            else{
                teamsForTopic.put("OverridePOTeamUUID", "");
                teamsForTopic.put("OverridePOTeamName", "");
                log.info("Removing Override PO Team as selected team was inactive.");
            }

        }

        if (!teamsForTopic.isEmpty()) {
            camundaClient.updateTask(stageUUID, teamsForTopic);
            caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);
        }

        log.debug("######## Updated Team Selection ########");
    }

    public void updatePOTeamSelection(String caseUUIDString, String stageUUIDString, String privateOfficeUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> teamsForTopic = new HashMap<>();

        if (StringUtils.hasText(privateOfficeUUIDString)) {
            log.info("Overwriting po team with {}", privateOfficeUUIDString);
            TeamDto pOTeam = infoClient.getTeam(UUID.fromString(privateOfficeUUIDString));
            teamsForTopic.put("PrivateOfficeOverridePOTeamUUID", pOTeam.getUuid().toString());
            teamsForTopic.put("PrivateOfficeOverridePOTeamName", pOTeam.getDisplayName());
        }

        if (!teamsForTopic.isEmpty()) {
            camundaClient.updateTask(stageUUID, teamsForTopic);
            caseworkClient.updateCase(caseUUID, stageUUID, teamsForTopic);
        }

        log.debug("######## Updated Team Selection at PO ########");
    }

    public void updateTeamByStageAndTexts(String caseUUIDString, String stageUUIDString, String stageType, String teamUUIDKey, String teamNameKey, String... texts) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> teamForText = caseworkClient.updateTeamByStageAndTexts(caseUUID, stageUUID, stageType, teamUUIDKey, teamNameKey, texts);
        camundaClient.updateTask(stageUUID, teamForText);
        caseworkClient.updateCase(caseUUID, stageUUID, teamForText);


        log.debug("######## Updated Team For Stage And Text ########");
    }

    public void updateValue(String caseUUIDString, String stageUUIDString, String... argPairs) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        Map<String, String> data = parseArgPairs(argPairs);

        if (!CollectionUtils.isEmpty(data)) {
            camundaClient.updateTask(stageUUID, data);
            caseworkClient.updateCase(caseUUID, stageUUID, data);
        }
    }

    public void saveTodaysDateToCaseVariable(String caseUUIDString, String stageUUIDString, String destination) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String todaysDateFormatted = simpleDateFormat.format(Date.from(clock.instant()));

        updateCaseValue(caseUUIDString, stageUUIDString, destination, todaysDateFormatted);
    }

    public void updateCaseValue(String caseUUIDString, String stageUUIDString, String... argPairs) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        Map<String, String> data = parseArgPairs(argPairs);

        if (!CollectionUtils.isEmpty(data)) {
            caseworkClient.updateCase(caseUUID, stageUUID, data);
        }
    }

    private Map<String, String> parseArgPairs(String... argPairs) {
        if (argPairs.length % 2 == 1) {
            throw new ApplicationExceptions.InvalidMethodArgumentException("Even number of arguments expected");
        }

        Map<String, String> data = new HashMap<>();

        for (int i = 0; i < argPairs.length; i = i + 2) {
            String key = argPairs[i];
            String value = argPairs[i + 1];
            log.info("Update {} key to {} value", key, value);
            data.put(key, value);
        }

        return data;
    }

    public void blankCaseValues(String caseUUIDString, String stageUUIDString, String... keys) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        if (keys != null && keys.length > 0) {
            log.info("Update {} keys to blank value", Arrays.toString(keys));
            Map<String, String> data = Arrays.stream(keys)
                    .collect(Collectors.toMap(p -> p, p -> ""));
            caseworkClient.updateCase(caseUUID, stageUUID, data);
        }
    }

    public void updateAllocationNote(String caseUUIDString, String stageUUIDString, String allocationNote, String allocationNoteType) {
        log.debug("######## Save Allocation Note ########");
        caseworkClient.createCaseNote(UUID.fromString(caseUUIDString), allocationNoteType, allocationNote);
        log.info("Adding Casenote to Case: {}", caseUUIDString);
    }

    /**
     *
     * @param caseUUIDString The UUID string for the case that is being updated
     * @param stageUUIDString
     * @param allocationNote the content of the allocation note
     * @param allocationNoteType The type of allocation note, usually REJECT or ALLOCATE
     * @param newTeamUUID The destination of the allocation
     * @param allocationStageUUID The stage at which the allocation request was made
     */
    public void updateAllocationNoteWithDetails(String caseUUIDString, String stageUUIDString, String allocationNote, String allocationNoteType,
                                                String newTeamUUID, String allocationStageUUID ) {

        NoteDetails noteDetails = fetchNoteDetails(caseUUIDString, allocationStageUUID);

        if (allocationNoteType.equals("ALLOCATE")){
            String newTeamName = infoClient.getTeam(UUID.fromString(newTeamUUID)).getDisplayName();
            allocationNote = noteDetails.getOldTeamName() + " allocated case to " + newTeamName + " at " +
                    noteDetails.getStageTypeDto().getDisplayName() + " stage: " + allocationNote;
        } else {
            allocationNote = noteDetails.getOldTeamName() + " rejected case at " +
                    noteDetails.getStageTypeDto().getDisplayName() + " stage: " + allocationNote;
        }

        log.debug("######## Save Allocation Note ########");
        caseworkClient.createCaseNote(UUID.fromString(caseUUIDString), allocationNoteType, allocationNote);
        log.info("Adding Casenote to Case: {}", caseUUIDString);
    }

    public void createAllocationDetailsNote(String caseUUIDString, String newTeamUUID, String allocationStageUUID) {

        log.debug("Request received to add Allocation details note to Case: {}", caseUUIDString);

        NoteDetails noteDetails = fetchNoteDetails(caseUUIDString, allocationStageUUID);
        String newTeamName = infoClient.getTeam(UUID.fromString(newTeamUUID)).getDisplayName();

        String allocationDetailsNote = noteDetails.getOldTeamName() +
                " allocated case to " + newTeamName + " at " + noteDetails.getStageTypeDto().getDisplayName() + " stage.";

        caseworkClient.createCaseNote(UUID.fromString(caseUUIDString), NoteType.ALLOCATE.toString(), allocationDetailsNote);
        log.info("Added Allocation Details case note to Case: {}", caseUUIDString);
    }

    /**
     * Sets the specified variables to empty strings in Casework and removes them from Workflow
     * @param caseUUIDString the case UUID as a String
     * @param stageUUIDString the stage UUID as a String
     * @param variables the variables to wipe
     */
    public void wipeVariables(String caseUUIDString, String stageUUIDString, String... variables) {
        log.debug("######## Reject/Deallocate Case ########");
        blankCaseValues(caseUUIDString,stageUUIDString, variables);
        camundaClient.removeTaskVariables(UUID.fromString(stageUUIDString), variables);
    }

    public void createCaseNote(String caseUUIDString, String caseNote, String caseNoteType) {
        log.debug("######## Create Case Note ########");
        caseworkClient.createCaseNote(UUID.fromString(caseUUIDString), caseNoteType, caseNote);
        log.info("Adding new Case note to Case: {}", caseUUIDString);
    }

    public void createCaseConversionNote(String caseUUIDString, String stageUUIDString, String caseConversionNote) {
        log.debug("######## Create Case Conversion Note ########");
        String caseChangeNoteType = null;
        String dataValueCaseRefType = caseworkClient.getDataValue(caseUUIDString, "RefType");

        if (!dataValueCaseRefType.isEmpty()) {
            if (dataValueCaseRefType.equals("Ministerial")) {
                caseChangeNoteType = "CONVERTED_CASE_TO_MINISTERIAL";
            } else { // case type must be official
                caseChangeNoteType = "CONVERTED_CASE_TO_OFFICIAL";
            }
            caseworkClient.createCaseNote(UUID.fromString(caseUUIDString), caseChangeNoteType, caseConversionNote);
            log.info("Added Case Conversion Note to Case: {}", caseUUIDString);
        } else {
            log.info("Case Ref type not known, could not add Case Conversion Note to Case: {}", caseUUIDString);
        }

    }

    private UUID deriveTeamUUID(String caseUUIDString, String stageTypeString, String allocationTeamString) {
        UUID teamUUID;
        if (StringUtils.isEmpty(allocationTeamString)) {
            log.debug("Getting Team selection from Info Service for stage {} for case {}", stageTypeString, caseUUIDString);
            teamUUID = infoClient.getTeamForStageType(stageTypeString);
        } else {
            log.debug("Overriding Team selection with {} for stage {} for case {}", allocationTeamString, stageTypeString, caseUUIDString);
            teamUUID = UUID.fromString(allocationTeamString);
        }

        return teamUUID;

    }

    private UUID deriveUserUUID(String caseUUIDString, String stageTypeString, String allocatedUserId) {
        UUID userUUID = null;
        if (!StringUtils.isEmpty(allocatedUserId)) {
            log.debug("Assigning user {} to stage {} for case {}", allocatedUserId, stageTypeString, caseUUIDString);
            userUUID = UUID.fromString(allocatedUserId);
        }
        return userUUID;
    }

    private void recreateStage(String caseUUIDString, String stageUUIDString, String stageType, String allocationType, UUID teamUUID, UUID userUUID) {

        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        caseworkClient.recreateStage(caseUUID, stageUUID, stageType);

        log.debug("Stage already exists for case {}, assigning to team {}", caseUUID, teamUUID);
        caseworkClient.updateStageTeam(caseUUID, stageUUID, teamUUID, allocationType);

        if (userUUID != null) {
            caseworkClient.updateStageUser(caseUUID, stageUUID, userUUID);
        }

    }

    public void updateCount(String caseUUID, String variableName, int additive) {

        String dataValue = caseworkClient.getDataValue(caseUUID, variableName);

        int updatedValue;
        if (StringUtils.hasText(dataValue)) {
            updatedValue = NumberUtils.parseInt(dataValue) + additive;
        } else {
            updatedValue = additive;
        }

        caseworkClient.updateDataValue(caseUUID, variableName, String.valueOf(updatedValue));

    }

    public void unallocateUserFromStage(String caseUUIDString, String stageUUIDString) {
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);

        caseworkClient.updateStageUser(caseUUID, stageUUID, null);
    }

    public void allocateUserToStage(String caseUUIDString, String stageUUIDString, String userUUIDString) {
        
        UUID caseUUID = UUID.fromString(caseUUIDString);
        UUID stageUUID = UUID.fromString(stageUUIDString);
        UUID userUUID = UUID.fromString(userUUIDString);

        log.debug("Allocating user {}, to stage {} for case {}", userUUID, stageUUID, caseUUID);

        caseworkClient.updateStageUser(caseUUID, stageUUID, userUUID);
    }

    public Date calculateDeadline(String caseType, int workingDays) {
        LocalDate startDate = LocalDate.now(clock);
        return infoClient.calculateDeadline(caseType, startDate, workingDays);
    }

    private NoteDetails fetchNoteDetails(String caseUUIDString, String allocationStageUUID) {
        String oldTeamName = infoClient.getTeam(caseworkClient.getStageTeam(UUID.fromString(caseUUIDString), UUID.fromString(allocationStageUUID))).getDisplayName();
        String caseworkStageType = caseworkClient.getStageType(UUID.fromString(caseUUIDString), UUID.fromString(allocationStageUUID));
        StageTypeDto stageTypeDto = infoClient.getAllStageTypes()
                .stream()
                .filter(st -> st.getType().equals(caseworkStageType))
                .findFirst()
                .get();

        return new NoteDetails(oldTeamName, stageTypeDto);
    }

    @AllArgsConstructor
    @Getter
    private static class NoteDetails {
        private final String oldTeamName;
        private final StageTypeDto stageTypeDto;
    }
}
