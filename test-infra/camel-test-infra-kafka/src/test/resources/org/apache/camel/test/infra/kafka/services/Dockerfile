ARG FROMIMAGE
FROM $FROMIMAGE
ARG KAFKA_VERSION
ENV KAFKA_VERSION ${KAFKA_VERSION:-2.8.0}
ARG KAFKA_MINOR_VERSION
ENV KAFKA_MINOR_VERSION ${KAFKA_MINOR_VERSION:-2.13}

RUN sudo yum -y install java

RUN curl --retry 3 --retry-connrefused https://downloads.apache.org/kafka/${KAFKA_VERSION}/kafka_${KAFKA_MINOR_VERSION}-${KAFKA_VERSION}.tgz -o kafka.tgz && \
        mkdir -p kafka && tar --strip-components=1 -xvf kafka.tgz -C kafka && rm kafka.tgz

WORKDIR kafka

RUN pwd && ls && ./bin/kafka-storage.sh random-uuid >> random-uuid && ./bin/kafka-storage.sh format -t $(cat random-uuid) -c ./config/kraft/server.properties
RUN echo 'auto.create.topics.enable=true' >> ./config/kraft/server.properties

EXPOSE 9092 9093

CMD [ "sh", "-c", "./bin/kafka-server-start.sh ./config/kraft/server.properties" ]