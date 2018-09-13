package uk.gov.digital.ho.hocs.workflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import uk.gov.digital.ho.hocs.workflow.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_data")
@Where(clause = "not deleted")
@NoArgsConstructor
public class DocumentData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    @Getter
    private DocumentType type;

    @Column(name = "display_name")
    @Getter
    private String name;

    @Column(name = "orig_link")
    @Getter
    private String fileLink;

    @Column(name = "pdf_link")
    @Getter
    private String pdfLink;

    @Column(name = "status")
    @Getter
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "created")
    @Getter
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "deleted")
    @Getter
    @Setter
    private Boolean deleted = Boolean.FALSE;

    public DocumentData(UUID caseUUID, DocumentType type, String name) {
        if (caseUUID == null || type == null || name == null) {
            throw new EntityCreationException("Cannot create DocumentData(%s, %s, %s).", caseUUID, type, name);
        }
        this.uuid = UUID.randomUUID();
        this.type = type;
        this.name = name;
        this.caseUUID = caseUUID;
    }

    public void update(String fileLink, String pdfLink, DocumentStatus status) {
        if (fileLink == null || status == null) {
            throw new EntityCreationException("Cannot call DocumentData.update(%s, %s, %s).", fileLink, pdfLink, status);
        }
        this.fileLink = fileLink;
        this.pdfLink = pdfLink;
        this.status = status;
    }
}