package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import uk.gov.digital.ho.hocs.workflow.util.UuidUtils;

import java.util.*;

public class HocsFormAccordion extends HocsFormField {

    public HocsFormAccordion(List<HocsFormSection> hocsFormSections) {
        this.props = new HashMap<>();
        //props.put("label", "accordion");
        this.component = "accordion";
        this.props.put("sections", hocsFormSections);
    }

    public static List<HocsFormField> loadFormAccordions(List<HocsFormField> fields) {
        List<HocsFormField> returnFields = new ArrayList();
        List<HocsFormSection> accordion = null;
        HocsFormSection section = null;
        List<HocsFormField> expandableItems = null;

        for (HocsFormField field : fields) {
            if (field.getComponent().equals("accordion")) {
                expandableItems = null;
                if (accordion == null) {
                    accordion = new ArrayList();

                    if (field.getProps().get("name") == null) {
                        field.getProps().put("name", UUID.randomUUID());
                    }

                    returnFields.add(new HocsFormAccordion(accordion));
                }
                section = new HocsFormSection(field);
                accordion.add(section);
            } else if (field.getComponent().equals("accordion-end")) {
                expandableItems = null;
                section = null;
                accordion = null;
            } else if (field.getComponent().equals("expandable-checkbox")) {
                expandableItems = new ArrayList<HocsFormField>();
                field.props.put("items", expandableItems);
                if (section != null) {
                    section.items.add(field);
                } else {
                    returnFields.add(field);
                }
            } else if (field.getComponent().equals("expandable-end")) {
                expandableItems = null;
            } else {
                if (expandableItems != null) {
                    expandableItems.add(field);
                } else if (section != null) {
                    section.items.add(field);
                } else {
                    returnFields.add(field);
                }
            }
        }
        return returnFields;
    }
}
