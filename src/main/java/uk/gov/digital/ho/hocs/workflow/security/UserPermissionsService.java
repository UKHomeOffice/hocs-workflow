package uk.gov.digital.ho.hocs.workflow.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.PermissionDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;

import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.SECURITY_UNAUTHORISED;

@Service
@Slf4j
public class UserPermissionsService {

    private final RequestData requestData;

    private final InfoClient infoClient;

    public UserPermissionsService(RequestData requestData, InfoClient infoClient) {
        this.requestData = requestData;
        this.infoClient = infoClient;
    }

    public UUID getUserId() {
        return UUID.fromString(requestData.userId());
    }

    public AccessLevel getMaxAccessLevel(String caseType) {

        return getUserPermission().stream().filter(permission -> permission.getCaseTypeCode().equals(caseType)).map(
            PermissionDto::getAccessLevel).max(Comparator.comparing(AccessLevel::getLevel)).orElseThrow(
            () -> new SecurityExceptions.PermissionCheckException("No permissions found for case type",
                SECURITY_UNAUTHORISED));
    }

    public Set<UUID> getUserTeams() {
        String[] groups = requestData.groupsArray();
        return Arrays.stream(groups).map(this::getUUIDFromBase64).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public boolean isUserOnTeam(UUID teamUUID) {
        return getUserTeams().contains(teamUUID);
    }

    public Set<String> getUserCaseTypes() {
        return getUserPermission().stream().map(PermissionDto::getCaseTypeCode).collect(Collectors.toSet());
    }

    Set<PermissionDto> getUserPermission() {
        Set<TeamDto> teamDtos = infoClient.getTeams();
        Set<UUID> userTeams = getUserTeams();

        return teamDtos.stream().filter(t -> userTeams.contains(t.getUuid())).flatMap(
            t -> t.getPermissionDtos().stream()).collect(Collectors.toSet());
    }

    public Set<String> getCaseTypesIfUserTeamIsCaseTypeAdmin() {
        return getUserPermission().stream().filter(
            permission -> permission.getAccessLevel() == AccessLevel.CASE_ADMIN).map(
            PermissionDto::getCaseTypeCode).collect(Collectors.toSet());
    }

    private UUID getUUIDFromBase64(String uuid) {
        if (uuid.startsWith("/")) {
            uuid = uuid.substring(1);
        }
        try {
            return Base64UUID.Base64StringToUUID(uuid);
        } catch (BufferUnderflowException e) {
            return null;
        }
    }

}
