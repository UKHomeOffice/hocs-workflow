package uk.gov.digital.ho.hocs.workflow.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.application.RequestData;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.PermissionDto;
import uk.gov.digital.ho.hocs.workflow.client.infoclient.dto.TeamDto;
import java.nio.BufferUnderflowException;
import java.util.*;
import java.util.stream.Collectors;
import static uk.gov.digital.ho.hocs.workflow.application.LogEvent.SECURITY_UNAUTHORISED;

@Service
@Slf4j
public class UserPermissionsService {

    private RequestData requestData;
    private InfoClient infoClient;

    @Autowired
    public UserPermissionsService(RequestData requestData, InfoClient infoClient) {
        this.requestData = requestData;
        this.infoClient = infoClient;
    }

    public UUID getUserId() {
        return UUID.fromString(requestData.userId());
    }

    public AccessLevel getMaxAccessLevel(String caseType) {
        Set<PermissionDto> permissionDtos = getUserPermission();
        Optional<PermissionDto> maxPermission = permissionDtos.stream()
                .filter(e-> e.getCaseTypeCode().equals(caseType))
                .max(Comparator.comparing(e -> e.getAccessLevel()));

        maxPermission.ifPresent(p -> log.info("Max permission case type: {}, permission: {}", p.getCaseTypeCode(), p.getAccessLevel().toString()));
        return maxPermission.orElseThrow(
                () -> new SecurityExceptions.PermissionCheckException("No permissions found for case type", SECURITY_UNAUTHORISED)
        ).getAccessLevel();
    }


    public Set<UUID> getUserTeams() {
        String[] groups = requestData.groupsArray();
        Set<UUID> userTeams = Arrays.stream(groups)
                .map(group -> getUUIDFromBase64(group))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if(log.isDebugEnabled()) {
            userTeams.forEach(t -> log.debug("User team: {}", t));
        }
        log.info("Found {} User teams", userTeams.size());
        return userTeams;
    }

    public Set<String> getUserCaseTypes() {
        Set<String> userCaseTypes = getUserPermission().stream()
                .map(p -> p.getCaseTypeCode())
                .collect(Collectors.toSet());
        if(log.isDebugEnabled()) {
            userCaseTypes.forEach(c -> log.debug("User case type: {}", c));
        }
        log.info("Found {} User case types", userCaseTypes.size());
        return userCaseTypes;
    }

    Set<PermissionDto> getUserPermission() {
        Set<TeamDto> teamDtos = infoClient.getTeams();
        Set<UUID> userTeams = getUserTeams();
        Set<PermissionDto> set = teamDtos.stream()
                .filter(t -> userTeams.contains(t.getUuid()))
                .flatMap(t -> t.getPermissionDtos().stream())
                .collect(Collectors.toSet());
        log.info("{} User permissions", set.size());

        return set;
    }


    private UUID getUUIDFromBase64(String uuid) {
        if(uuid.startsWith("/")) {
            uuid = uuid.substring(1);
        }
        try {
            return Base64UUID.Base64StringToUUID(uuid);
        }
        catch (BufferUnderflowException e) {
            return null;
        }
    }
}