FROM quay.io/ukhomeofficedigital/hocs-base-image:4.1.6 as builder

WORKDIR /builder

COPY ./build/libs/hocs-*.jar .

RUN java -Djarmode=layertools -jar hocs-*.jar extract

FROM quay.io/ukhomeofficedigital/hocs-base-image:4.1.6

WORKDIR /app

COPY --from=builder --chown=user_hocs:group_hocs ./builder/spring-boot-loader/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./builder/dependencies/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./builder/application/ ./

USER 10000

ENTRYPOINT exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher
