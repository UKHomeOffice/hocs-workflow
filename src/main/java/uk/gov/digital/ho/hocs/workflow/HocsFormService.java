package uk.gov.digital.ho.hocs.workflow;


import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsForm;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsFormAction;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsFormField;
import uk.gov.digital.ho.hocs.workflow.model.forms.HocsSchema;

import java.util.*;

@Service
public class HocsFormService {

    private Map<String,HocsForm> forms = new HashMap<>();

    public HocsFormService(){
        forms.put("INITIAL_DECISION", getInitialDecision());
        forms.put("DEADLINES", getDeadlinesForm());
        forms.put("TOPICS", getTopics());
        forms.put("ANSWERING",getAllocateForm());
        forms.put("ALLOCATION_NOTE",getAllocationNote());
    }

    public HocsForm getStage(String form){
        return forms.get(form);
    }

    private HocsForm getInitialDecision() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Policy Response");
        choice1.put("value", "PR");
        choice1.put("checked", "checked");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "FAQ");
        choice2.put("value", "FAQ");

        Map<String,String> choice3 = new HashMap<>();
        choice3.put("label", "Refer To OGD");
        choice3.put("value", "OGD");

        Map<String,String> choice4 = new HashMap<>();
        choice4.put("label", "No Reply Needed");
        choice4.put("value", "NRN");
        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        choices.add(choice3);
        choices.add(choice4);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "InitialDecision");
        properties4.put("label", "Initial Decision");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Initial Decision", "Next", formFields);

        Map<String, Object> data = new HashMap<>();
        HocsForm form1 = new HocsForm(schema1, data);

        return form1;
    }

    private HocsForm getAllocateForm() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "MarkupUnit");
        properties4.put("label", "Markup Unit");

        HocsFormField fieldFour = new HocsFormField("dropdown", validationList, properties4);

        Map<String,Object> properties5 = new HashMap<>();
        properties5.put("name", "MarkupTeam");
        properties5.put("label", "Markup Team");

        HocsFormField fieldFive = new HocsFormField("dropdown", validationList, properties5);

        Map<String,Object> properties6 = new HashMap<>();
        properties6.put("name", "SignOffMinister");
        properties6.put("label", "Sign-Off Minister");

        HocsFormField fieldSix = new HocsFormField("dropdown", validationList, properties6);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);
        formFields.add(fieldSix);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Answering", "Next", formFields);

        Map<String, Object> data = new HashMap<>();
        HocsForm form1 = new HocsForm(schema1, data);

        return form1;
    }

    private HocsForm getDeadlinesForm() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties1 = new HashMap<>();
        properties1.put("name", "DateReceived");
        properties1.put("label", "Date Received");

        HocsFormField fieldOne = new HocsFormField("date", validationList, properties1);

        Map<String,Object> properties2 = new HashMap<>();
        properties2.put("name", "DraftingDeadline");
        properties2.put("label", "Drafting Deadline");

        HocsFormField fieldTwo = new HocsFormField("date", validationList, properties2);

        Map<String,Object> properties3 = new HashMap<>();
        properties3.put("name", "FinalDeadline");
        properties3.put("label", "Final Deadline");

        HocsFormField fieldThree = new HocsFormField("date", validationList, properties3);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldOne);
        formFields.add(fieldTwo);
        formFields.add(fieldThree);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Deadlines", "Next", formFields);

        Map<String, Object> data = new HashMap<>();
        data.put("DateReceived", "1988-04-14");

        HocsForm form1 = new HocsForm(schema1, data);

        return form1;
    }

    private HocsForm getAllocationNote(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "AllocationNote");
        properties7.put("label", "Allocation Note");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Allocation Note", "Submit", formFields);

        Map<String, Object> data = new HashMap<>();
        HocsForm form1 = new HocsForm(schema1, data);

        return form1;

    }

    private HocsForm getTopics(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "Topics");
        properties7.put("label", "Topics");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Topics", "Next", formFields);

        Map<String, Object> data = new HashMap<>();
        HocsForm form1 = new HocsForm(schema1, data);

        return form1;

    }

}
