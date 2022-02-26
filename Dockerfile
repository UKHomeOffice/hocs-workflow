FROM quay.io/ukhomeofficedigital/hocs-base-image as builder

COPY build/libs/hocs-workflow.jar ./
COPY scripts/run.sh ./

RUN java -Djarmode=layertools -jar hocs-workflow.jar extract

FROM quay.io/ukhomeofficedigital/hocs-base-image

COPY --from=builder --chown=user_hocs:group_hocs /app/run.sh ./
COPY --from=builder --chown=user_hocs:group_hocs /app/spring-boot-loader/ ./
COPY --from=builder --chown=user_hocs:group_hocs /app/dependencies/ ./
COPY --from=builder --chown=user_hocs:group_hocs /app/application/ ./

CMD ["sh", "/app/run.sh"]
