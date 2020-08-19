FROM openjdk:14-jdk-alpine

ENV VERTICLE_FILE build/libs/api-gateway-all.jar

# Set the location of the verticles
ENV VERTICLE_HOME /opt/verticles

EXPOSE 443

COPY $VERTICLE_FILE $VERTICLE_HOME/
COPY src/config/docker.json $VERTICLE_HOME/


WORKDIR $VERTICLE_HOME
ENTRYPOINT ["sh", "-c"]
CMD ["java -Xmx384m -Dvertx.hazelcast.config=classpath:cluster-aws.xml -jar api-gateway-all.jar -cluster -ha -conf docker.json"]
