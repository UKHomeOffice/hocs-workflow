package uk.gov.digital.ho.hocs.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Correspondent {

    private CorrespondentType correspondentType;

    private String fullname;

    private String postcode;

    private String address1;

    private String address2;

    private String address3;

    private String country;

    private String telephone;

    private String email;

    private String reference;
}