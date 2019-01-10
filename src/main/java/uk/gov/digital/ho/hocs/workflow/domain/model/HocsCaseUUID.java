package uk.gov.digital.ho.hocs.workflow.domain.model;

import java.util.UUID;

public class HocsCaseUUID {

    private HocsCaseUUID() { }

    public static UUID randomUUID(CaseDataType type){
        String uuid = UUID.randomUUID().toString().substring(0,33);
        String hocsUUID = uuid.concat(type.getShortCode());
        return UUID.fromString(hocsUUID);
    }

    public static CaseDataType getCaseDataType(UUID uuid) {
        String caseValue = uuid.toString().substring(34);
        return findCaseDataType(caseValue);
    }

    private static CaseDataType findCaseDataType(String caseValue) {
        for(CaseDataType cd : CaseDataType.values()) {
            if (cd.getShortCode().equals(caseValue)) {
                return cd;
            }
        }
        return null;
    }
}
