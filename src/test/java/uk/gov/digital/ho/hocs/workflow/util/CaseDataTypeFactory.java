package uk.gov.digital.ho.hocs.workflow.util;

import uk.gov.digital.ho.hocs.workflow.api.dto.CaseDataType;

public class CaseDataTypeFactory {

    /**
     * Convenience constructor scoped to test classes.
     * @param type
     * @param shortCode
     * @return
     */
    public static CaseDataType from(String type, String shortCode) {
        return new CaseDataType(type, shortCode);
    }

}
