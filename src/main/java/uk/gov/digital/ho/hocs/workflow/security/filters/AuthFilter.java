package uk.gov.digital.ho.hocs.workflow.security.filters;

import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.workflow.security.AccessLevel;
import uk.gov.digital.ho.hocs.workflow.security.SecurityExceptions;

public interface AuthFilter {

    String getKey();

    Object applyFilter(ResponseEntity<?> responseEntityToFilter,
                       AccessLevel userAccessLevel,
                       Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException;

}
