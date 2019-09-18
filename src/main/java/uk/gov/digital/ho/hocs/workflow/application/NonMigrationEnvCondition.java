package uk.gov.digital.ho.hocs.workflow.application;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;

public class NonMigrationEnvCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return Arrays.stream(context.getEnvironment().getActiveProfiles()).noneMatch(prof -> prof.equals("migration"));
    }
}