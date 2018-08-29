package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Deadline {

    private LocalDate date;

    private String type;
}
