FROM openjdk:8-jdk-alpine

ARG brokerId=1
ARG kafkaPath=1
RUN apk add bash -u

COPY kafka_2.11-2.0.0 /kafka/

RUN sed -i s/localhost:2181/zookeeper:2181/g /kafka/config/server.properties && \
    sed -i s/broker.id=0/broker.id=${brokerId}/g /kafka/config/server.properties

RUN ls /kafka/bin

EXPOSE 2181
