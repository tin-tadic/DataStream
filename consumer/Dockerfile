FROM openjdk:11-jre-slim
VOLUME /tmp
EXPOSE 8080
RUN mkdir -p /app/
ADD consumer/target/consumer-*.jar /app/consumer.jar
ARG ENVIRONMENT='default'
ENV JAVA_OPTS='-XX:+UnlockExperimentalVMOptions'
ENV PROFILE=${ENVIRONMENT}
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/consumer.jar --spring.profiles.active=$PROFILE"]