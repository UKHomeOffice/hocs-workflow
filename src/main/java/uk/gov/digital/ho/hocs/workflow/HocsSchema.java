package uk.gov.digital.ho.hocs.workflow;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class HocsSchema {

    public HocsFormAction action;

    public String title;

    public String defaultActionLabel;

    public List<HocsFormField> fields;
}