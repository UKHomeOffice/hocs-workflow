package uk.gov.digital.ho.hocs.workflow.util;

import org.springframework.util.StringUtils;

public class UuidUtils {

    private static String UUID_REGEX = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";

    public static boolean isUUID(String uuid) {
        return StringUtils.hasText(uuid) && uuid.matches(UUID_REGEX);
    }
}
