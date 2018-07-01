package uk.gov.digital.ho.hocs.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.workflow.caseworkClient.DocumentSummary;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AddDocumentRequest {

    private DocumentSummary documentSummary;
}
