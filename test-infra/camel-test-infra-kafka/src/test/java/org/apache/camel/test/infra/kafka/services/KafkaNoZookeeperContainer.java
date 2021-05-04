package org.apache.camel.test.infra.kafka.services;

import org.apache.camel.test.infra.common.TestUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class KafkaNoZookeeperContainer extends GenericContainer<KafkaNoZookeeperContainer> {
    private static final int DEFAULT_LISTENER_PORT = 9092;
    private static final int DEFAULT_CONTROLLER_PORT = 9093;
    private static final String FROM_IMAGE_NAME = "fedora:33";
    private static final String FROM_IMAGE_ARG = "FROMIMAGE";

    public KafkaNoZookeeperContainer() {
        super(new ImageFromDockerfile("localhost/apache-kafka-nozookeeper:camel", false)
                .withFileFromClasspath("Dockerfile",
                        "org/apache/camel/test/infra/kafka/services/Dockerfile")
                .withBuildArg(FROM_IMAGE_ARG, TestUtils.prependHubImageNamePrefixIfNeeded(FROM_IMAGE_NAME)));

        withExposedPorts(DEFAULT_LISTENER_PORT, DEFAULT_CONTROLLER_PORT);

        waitingFor(Wait.forListeningPort());
    }

    public int listenerPort() {
        return getMappedPort(DEFAULT_LISTENER_PORT);
    }

    public String getBootstrapServers() {
        return String.format("PLAINTEXT://%s:%s", this.getHost(), listenerPort());
    }
}
