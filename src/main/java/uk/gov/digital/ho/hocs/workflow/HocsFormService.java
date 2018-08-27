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

        // Shared
        forms.put("DEADLINES", getDeadlinesForm());
        forms.put("ALLOCATION_NOTE",getAllocationNote(false, "Allocation", "Allocation Note"));

        // DCU

        //// Create
        forms.put("DCU_CORRESPONDENCE_DETAILS", getDataInput());
        forms.put("DCU_CORRESPONDENT_QUESTION", getCorrespondentMemberQuestion());
        forms.put("DCU_ADD_PUBLIC_CORRESPONDENT", getAddCorrespondentPublic());
        forms.put("DCU_ADD_MEMBER_CORRESPONDENT", getAddCorrespondentMember());
        forms.put("DCU_ADD_ANOTHER_CORRESPONDENT", getAddAnotherCorrespondent());
        forms.put("DCU_ADD_REFERENCE", getAddReference());
        forms.put("DCU_ADD_ANOTHER_REFERENCE", getAddAnotherReference());
        forms.put("DCU_DATA_INPUT_SUMMARY", getDataInputSummary());


        //// Draft
        forms.put("DCU_INITIAL_DRAFT_DECISION", getInitialDraftConfirmation());
        forms.put("DCU_REJECTION_NOTE", getRejectionNote());
        forms.put("DCU_RESPONSE_CHANNEL", getResponseChannel());
        forms.put("DCU_PHONECALL_NOTE", getPhonecallNote());
        forms.put("DCU_UPLOAD_DOCUMENT", getUploadDraftDocument());
        forms.put("DCU_OFFLINE_QA_DECISION", getOfflineQADecision());
        forms.put("DCU_OFFLINE_QA_DETAILS", getOfflineQADetails());

        forms.put("MARKUP_DECISION", getMarkupDecision());
        forms.put("TOPICS", getTopics());
        forms.put("TRANSFER_CONFIRMATION", getTransferConfirmation());
        forms.put("NO_REPLY_NEEDED_CONFIRMATION", getNoReplyNeededConfirmation());
        forms.put("APPROVE_DATA_INPUT_QA", getAcceptDataInputQA());

        // UKVI
        forms.put("INITIAL_DECISION_UKVI", getInitialDecisionUKVI());
        forms.put("OWNING_MEMBER", getOwningMember());
        forms.put("ANSWERING",getAllocateForm());
    }



    public HocsForm getForm(String form){
        return forms.get(form);
    }

    private HocsForm getDataInput() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties1 = new HashMap<>();
        properties1.put("name", "DateReceived");
        properties1.put("label", "When was the correspondence received?");

        HocsFormField fieldOne = new HocsFormField("date", validationList, properties1);

        Map<String,Object> properties2 = new HashMap<>();
        properties2.put("name", "DateOfCorrespondence");
        properties2.put("label", "When was the correspondence sent?");

        HocsFormField fieldTwo = new HocsFormField("date", validationList, properties2);

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Email");
        choice1.put("value", "EMAIL");
        choice1.put("checked", "checked");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "Post");
        choice2.put("value", "POST");

        Map<String,String> choice3 = new HashMap<>();
        choice3.put("label", "Phone");
        choice3.put("value", "PHONE");

        Map<String,String> choice4 = new HashMap<>();
        choice4.put("label", "No. 10");
        choice4.put("value", "NO10");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        choices.add(choice3);
        choices.add(choice4);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "OriginalChannel");
        properties4.put("label", "How was the correspondence received?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String,String> choice7 = new HashMap<>();
        choice7.put("label", "Yes");
        choice7.put("value", "TRUE");

        Map<String,String> choice8 = new HashMap<>();
        choice8.put("label", "No");
        choice8.put("value", "FALSE");
        choice8.put("checked", "checked");

        List<Map<String, String>> choices7 = new ArrayList<>();
        choices7.add(choice7);
        choices7.add(choice8);

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "CopyNumberTen");
        properties7.put("label", "Should a copy be sent to Number 10?");
        properties7.put("choices", choices7);

        HocsFormField fieldSeven = new HocsFormField("radio", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldTwo);
        formFields.add(fieldOne);
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondence Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getCorrespondentMemberQuestion() {

        List<String> validationList = new ArrayList<>();
        validationList.add("required");


        Map<String,String> choice3 = new HashMap<>();
        choice3.put("label", "Yes");
        choice3.put("value", "TRUE");

        Map<String,String> choice4 = new HashMap<>();
        choice4.put("label", "No");
        choice4.put("value", "FALSE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice3);
        choices.add(choice4);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "CorrespondentIsMember");
        properties4.put("label", "Is the correspondent a member of a Parliament or Assembly?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String,Object> properties5 = new HashMap<>();
        properties5.put("children", "These are MPs, MSPs, AMs, MLAs or British MEPs.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);


        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondent Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getAddCorrespondentPublic() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties1 = new HashMap<>();
        properties1.put("name", "CTitle");
        properties1.put("label", "Title");

        HocsFormField fieldOne = new HocsFormField("text", new ArrayList<>(), properties1);

        Map<String,Object> properties2 = new HashMap<>();
        properties2.put("name", "CFirstName");
        properties2.put("label", "First Name");

        HocsFormField fieldTwo = new HocsFormField("text", validationList, properties2);

        Map<String,Object> properties3 = new HashMap<>();
        properties3.put("name", "CLastName");
        properties3.put("label", "Last Name");

        HocsFormField fieldThree = new HocsFormField("text", validationList, properties3);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "CPostcode");
        properties4.put("label", "Postcode");

        HocsFormField fieldFour = new HocsFormField("text", validationList, properties4);

        Map<String,Object> properties5 = new HashMap<>();
        properties5.put("name", "CAddressOne");
        properties5.put("label", "Building");

        HocsFormField fieldFive = new HocsFormField("text", validationList, properties5);

        Map<String,Object> properties9 = new HashMap<>();
        properties9.put("name", "CAddressTwo");
        properties9.put("label", "Street");

        HocsFormField fieldNine = new HocsFormField("text", new ArrayList<>(), properties9);

        Map<String,Object> properties6 = new HashMap<>();
        properties6.put("name", "CEmail");
        properties6.put("label", "Email Address");

        HocsFormField fieldSix = new HocsFormField("text", new ArrayList<>(), properties6);

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "CPhone");
        properties7.put("label", "Telephone");

        HocsFormField fieldSeven = new HocsFormField("text", new ArrayList<>(), properties7);

        Map<String,Object> properties8 = new HashMap<>();
        properties8.put("name", "CTownOrCity");
        properties8.put("label", "Town or City");

        HocsFormField fieldEight = new HocsFormField("text", new ArrayList<>(), properties8);

        Map<String,Object> properties10 = new HashMap<>();
        List<String> help = new ArrayList<>();
        help.add("Add as many details as you can for the correspondent.");
        help.add("If there are more, you will be able to add additional correspondents after this.");
        properties10.put("children", help );

        HocsFormField fieldTen = new HocsFormField("inset", new ArrayList<>(), properties10);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldTen);
        formFields.add(fieldOne);
        formFields.add(fieldTwo);
        formFields.add(fieldThree);
        formFields.add(fieldFive);
        formFields.add(fieldNine);
        formFields.add(fieldEight);
        formFields.add(fieldFour);
        formFields.add(fieldSeven);
        formFields.add(fieldSix);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondent Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getAddCorrespondentMember(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Mike Hunt");
        choice1.put("value", "12345");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "Amanda Hugankiss");
        choice2.put("value", "56789");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "Member");
        properties4.put("label", "Select the member from the list.");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("dropdown", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();

        formFields.add(fieldFour);
        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondent Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;

    }

    private HocsForm getAddAnotherCorrespondent(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Yes");
        choice1.put("value", "TRUE");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "No");
        choice2.put("value", "FALSE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "AdditionalCorrespondent");
        properties4.put("label", "Do you need to add another correspondent?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);


        List<HocsFormField> formFields = new ArrayList<>();

        formFields.add(fieldFour);
        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondent Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;

    }

    private HocsForm getAddReference() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties1 = new HashMap<>();
        properties1.put("name", "CorrespondenceReference");
        properties1.put("label", "Is there a reference?");

        HocsFormField fieldOne = new HocsFormField("text", new ArrayList<>(), properties1);

        Map<String,Object> properties5 = new HashMap<>();
        properties5.put("children", "If there are more, you will be able to add additional references after this.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldOne);
        formFields.add(fieldFive);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondence Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getAddAnotherReference(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Yes");
        choice1.put("value", "TRUE");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "No");
        choice2.put("value", "FALSE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "AdditionalReference");
        properties4.put("label", "Do you need to add another reference?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);


        List<HocsFormField> formFields = new ArrayList<>();

        formFields.add(fieldFour);
        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondence Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getDataInputSummary(){

        List<String> validationList = new ArrayList<>();

        Map<String,Object> properties1 = new HashMap<>();
        properties1.put("name", "DateReceived");
        properties1.put("label", "When was the correspondence received?");

        HocsFormField fieldOne = new HocsFormField("date", validationList, properties1);

        Map<String,Object> properties2 = new HashMap<>();
        properties2.put("name", "DateOfCorrespondence");
        properties2.put("label", "When was the correspondence sent?");

        HocsFormField fieldTwo = new HocsFormField("date", validationList, properties2);

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Email");
        choice1.put("value", "EMAIL");
        choice1.put("checked", "checked");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "Post");
        choice2.put("value", "POST");

        Map<String,String> choice3 = new HashMap<>();
        choice3.put("label", "Phone");
        choice3.put("value", "PHONE");

        Map<String,String> choice4 = new HashMap<>();
        choice4.put("label", "No. 10");
        choice4.put("value", "NO10");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        choices.add(choice3);
        choices.add(choice4);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "OriginalChannel");
        properties4.put("label", "How was the correspondence received?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String,String> choice7 = new HashMap<>();
        choice7.put("label", "Yes");
        choice7.put("value", "TRUE");

        Map<String,String> choice8 = new HashMap<>();
        choice8.put("label", "No");
        choice8.put("value", "FALSE");
        choice8.put("checked", "checked");

        List<Map<String, String>> choices7 = new ArrayList<>();
        choices7.add(choice7);
        choices7.add(choice8);

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "CopyNumberTen");
        properties7.put("label", "Should a copy be sent to Number 10?");
        properties7.put("choices", choices7);

        HocsFormField fieldSeven = new HocsFormField("radio", validationList, properties7);

        Map<String,Object> properties8 = new HashMap<>();
        properties8.put("name", "Reference");
        properties8.put("label", "Reference");
        properties8.put("action", "REFERENCE");
        properties8.put("hasRemoveLink", false);
        properties8.put("hasAddLink", false);

        HocsFormField fieldEight = new HocsFormField("entity-list", new ArrayList<>() , properties8);

        Map<String,Object> properties9 = new HashMap<>();
        properties9.put("name", "Correspondents");
        properties9.put("label", "Correspondent");
        properties9.put("action", "CORRESPONDENT");
        properties9.put("hasRemoveLink", false);
        properties9.put("hasAddLink", false);

        HocsFormField fieldNine = new HocsFormField("entity-list", new ArrayList<>() , properties9);

        List<HocsFormField> formFields = new ArrayList<>();

        formFields.add(fieldOne);
        formFields.add(fieldTwo);
        formFields.add(fieldFour);
        formFields.add(fieldSeven);
        formFields.add(fieldEight);
        formFields.add(fieldNine);
        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Record Correspondence Details", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getCorrespondentSummary(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");


        Map<String,Object> properties9 = new HashMap<>();
        properties9.put("name", "References");
        properties9.put("label", "Reference");
        properties9.put("action", "REFERENCE");
        properties9.put("hasRemoveLink", true);
        properties9.put("hasAddLink", true);

        HocsFormField fieldNine = new HocsFormField("entity-list", new ArrayList<>(), properties9);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldNine);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Data Input", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getTransferConfirmation() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Refer To OGD");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "Reject Transfer");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "TransferConfirmation");
        properties4.put("label", "Transfer Confirmation");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Transfer Confirmation", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getNoReplyNeededConfirmation() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Accept");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "Reject");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "NoReplyNeededConfirmation");
        properties4.put("label", "No Reply Needed Confirmation");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "No Reply Needed Confirmation", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getInitialDraftConfirmation() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Yes");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "No");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "InitialDraftDecision");
        properties4.put("label", "Is this correspondence best answered by your team?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String,Object> properties5 = new HashMap<>();
        properties5.put("children", "If you select 'No' you will be asked to explain why and the case will return to DCU.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Drafting Team Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getAcceptDataInputQA() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Accept");
        choice1.put("value", "ACCEPT");
        choice1.put("checked", "checked");

        Map<String,String> choice2 = new HashMap<>();
        choice2.put("label", "Reject");
        choice2.put("value", "REJECT");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "DataInputQADecision");
        properties4.put("label", "Data Input QA Decision");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Data Input QA Decision", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getMarkupDecision() {
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
        properties4.put("name", "MarkupDecision");
        properties4.put("label", "Markup Decision");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Markup Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getAllocateForm() {
        List<String> validationList = new ArrayList<>();
        //alidationList.add("required");

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

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Answering", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getOwningMember() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String, Object> properties = new HashMap<>();
        properties.put("name", "owningMember");
        properties.put("label", "Owning Member");

        HocsFormField dropDown = new HocsFormField("dropdown", validationList, properties);

        List<HocsFormField> fields = new ArrayList<>();
        fields.add(dropDown);

        HocsSchema schema = new HocsSchema(HocsFormAction.SUBMIT, "Owning Member", "Continue", fields);

        HocsForm form1 = new HocsForm(schema, "");
        return  form1;
    }

    private HocsForm getDeadlinesForm() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");


        Map<String,Object> properties2 = new HashMap<>();
        properties2.put("name", "DraftingDeadline");
        properties2.put("label", "Drafting Deadline");

        HocsFormField fieldTwo = new HocsFormField("date", validationList, properties2);

        Map<String,Object> properties3 = new HashMap<>();
        properties3.put("name", "FinalDeadline");
        properties3.put("label", "Final Deadline");

        HocsFormField fieldThree = new HocsFormField("date", validationList, properties3);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldTwo);
        formFields.add(fieldThree);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Deadlines", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getPhonecallNote(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "PhonecallNote");
        properties7.put("label", "Please summarise your call.");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        Map<String,Object> properties5 = new HashMap<>();
        properties5.put("children", "You can return to this page after your call.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFive);
        formFields.add(fieldSeven);


        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Drafting Team Decision", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;

    }

    private HocsForm getRejectionNote(){
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", "RejectionNote");
        properties7.put("label", "Please state why this should not be answered by your team.");

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Drafting Team Decision", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;

    }

    private HocsForm getAllocationNote(boolean required, String title, String question){
        List<String> validationList = new ArrayList<>();
        if(required) {
            validationList.add("required");
        }

        Map<String,Object> properties7 = new HashMap<>();
        properties7.put("name", title + "Note");
        properties7.put("label",  question);

        HocsFormField fieldSeven = new HocsFormField("text-area", validationList, properties7);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldSeven);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, title + " Note", "Finish", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

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

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Topics", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;

    }

    private HocsForm getInitialDecisionUKVI() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");

        Map<String,String> choice1 = new HashMap<>();
        choice1.put("label", "Send To Draft");
        choice1.put("value", "DRAFT");
        choice1.put("checked", "checked");

        Map<String,String> choice3 = new HashMap<>();
        choice3.put("label", "Refer To OGD");
        choice3.put("value", "OGD");

        Map<String,String> choice4 = new HashMap<>();
        choice4.put("label", "No Reply Needed");
        choice4.put("value", "NRN");
        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice3);
        choices.add(choice4);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "InitialDecision");
        properties4.put("label", "Initial Decision");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Initial Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }


    private HocsForm getResponseChannel() {
        List<String> validationList = new ArrayList<>();
        validationList.add("required");


        Map<String,String> choice3 = new HashMap<>();
        choice3.put("label", "Letter");
        choice3.put("value", "LETTER");

        Map<String,String> choice4 = new HashMap<>();
        choice4.put("label", "Phone");
        choice4.put("value", "PHONE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice3);
        choices.add(choice4);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "ResponseChannel");
        properties4.put("label", "How do you intend to respond?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        Map<String,Object> properties5 = new HashMap<>();
        properties5.put("children", "If you select 'Phone' you will be asked to summarise the call and then the case will close.");

        HocsFormField fieldFive = new HocsFormField("inset", new ArrayList<>(), properties5);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);
        formFields.add(fieldFive);

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Drafting Team Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getUploadDraftDocument() {

        List<HocsFormField> formFields = new ArrayList<>();

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Upload Draft", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getOfflineQADecision() {

        List<String> validationList = new ArrayList<>();
        validationList.add("required");


        Map<String,String> choice3 = new HashMap<>();
        choice3.put("label", "Yes");
        choice3.put("value", "TRUE");

        Map<String,String> choice4 = new HashMap<>();
        choice4.put("label", "No");
        choice4.put("value", "FALSE");

        List<Map<String, String>> choices = new ArrayList<>();
        choices.add(choice3);
        choices.add(choice4);

        Map<String,Object> properties4 = new HashMap<>();
        properties4.put("name", "OfflineQA");
        properties4.put("label", "Do you want to QA this offline?");
        properties4.put("choices", choices);

        HocsFormField fieldFour = new HocsFormField("radio", validationList, properties4);

        List<HocsFormField> formFields = new ArrayList<>();
        formFields.add(fieldFour);


        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Offline QA Decision", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

    private HocsForm getOfflineQADetails() {

        List<HocsFormField> formFields = new ArrayList<>();

        HocsSchema schema1 = new HocsSchema(HocsFormAction.SUBMIT, "Offline QA Details", "Continue", formFields);

        HocsForm form1 = new HocsForm(schema1, "");

        return form1;
    }

}
