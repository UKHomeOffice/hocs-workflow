package uk.gov.digital.ho.hocs.workflow.domain.model.forms;

import java.util.*;

public class HocsFormAccordion extends HocsFormField {

    public HocsFormAccordion(List<HocsFormSection> hocsFormSections) {
        this.props = new HashMap<>();
        //props.put("label", "accordion");
        props.put("name", UUID.randomUUID());
        this.component = "accordion";
        this.props.put("sections", hocsFormSections);
    }

    public static List<HocsFormField> loadFormAccordions(List<HocsFormField> fields) {
        List<HocsFormField> returnFields = new ArrayList();
        List<HocsFormSection> accordion = null;
        HocsFormSection section = null;

        for (HocsFormField field : fields) {
            if (field.getComponent().equals("accordion")) {
                if (accordion == null) {
                    accordion = new ArrayList();
                    returnFields.add(new HocsFormAccordion(accordion));
                }
                section = new HocsFormSection(field);
                accordion.add(section);
            } else if (field.getComponent().equals("accordion-end")) {
                section = null;
                accordion = null;
            } else {
                if (section != null) {
                    section.items.add(field);
                } else {
                    returnFields.add(field);
                }
            }
        }
        return returnFields;
    }
}
