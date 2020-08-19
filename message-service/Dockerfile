FROM openjdk:14-jdk-alpine

ENV VERTICLE_FILE build/libs/message-service-all.jar

# Set the location of the verticles
ENV VERTICLE_HOME /opt/verticles

EXPOSE 8003

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/config/docker.json $VERTICLE_HOME/


WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -Xmx512m -Dvertx.hazelcast.config=classpath:cluster-aws.xml -jar message-service-all.jar -cluster -ha -conf docker.json"]
