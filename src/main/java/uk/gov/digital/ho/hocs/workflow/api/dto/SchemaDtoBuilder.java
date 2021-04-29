package uk.gov.digital.ho.hocs.workflow.api.dto;

import java.util.List;
import java.util.UUID;

public final class SchemaDtoBuilder {
    private UUID uuid;
    private String stageType;
    private String type;
    private String title;
    private String defaultActionLabel;
    private boolean active;
    private List<FieldDto> fields;
    private List<SecondaryActionDto> secondaryActions;
    private Object props;
    private List<ValidationRuleDto> validationRules;

    private SchemaDtoBuilder() {
    }

    public static SchemaDtoBuilder aSchemaDto() {
        return new SchemaDtoBuilder();
    }

    public SchemaDtoBuilder withUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public SchemaDtoBuilder withStageType(String stageType) {
        this.stageType = stageType;
        return this;
    }

    public SchemaDtoBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public SchemaDtoBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public SchemaDtoBuilder withDefaultActionLabel(String defaultActionLabel) {
        this.defaultActionLabel = defaultActionLabel;
        return this;
    }

    public SchemaDtoBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }

    public SchemaDtoBuilder withFields(List<FieldDto> fields) {
        this.fields = fields;
        return this;
    }

    public SchemaDtoBuilder withSecondaryActions(List<SecondaryActionDto> secondaryActions) {
        this.secondaryActions = secondaryActions;
        return this;
    }

    public SchemaDtoBuilder withSecondaryActions(Object props) {
        this.props = props;
        return this;
    }

    public SchemaDto build() {
        return new SchemaDto(uuid, stageType, type, title, defaultActionLabel, active, fields, secondaryActions, props, validationRules);
    }
}
