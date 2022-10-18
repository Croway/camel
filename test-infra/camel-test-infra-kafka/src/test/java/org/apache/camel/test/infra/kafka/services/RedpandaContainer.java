package org.apache.camel.test.infra.kafka.services;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import com.github.dockerjava.api.command.CreateContainerCmd;

public class RedpandaContainer extends GenericContainer<RedpandaContainer> {
	public static final String DEFAULT_STRIMZI_CONTAINER = "vectorized/redpanda:v22.2.6";
	private static final String STRIMZI_CONTAINER
			= System.getProperty("itest.redpanda.container.image", DEFAULT_STRIMZI_CONTAINER);
	private static final int KAFKA_PORT = 9092;

	public RedpandaContainer(Network network, String name) {
		this(network, name, STRIMZI_CONTAINER);
	}

	public RedpandaContainer(Network network, String name, String containerName) {
		super(containerName);

		withEnv("LOG_DIR", "/tmp/logs");
		withExposedPorts(KAFKA_PORT);
		withEnv("KAFKA_ADVERTISED_LISTENERS", String.format("PLAINTEXT://%s:9092", getHost()));
		withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092");
		withNetwork(network);

		withCreateContainerCmdModifier(createContainerCmd -> setupContainer(name, createContainerCmd));

		withCommand("redpanda start " +
						"--overprovisioned " +
						"--smp 1  " +
						"--memory 1G " +
						"--reserve-memory 0M " +
						"--node-id 0 " +
						"--check=false");

		waitingFor(Wait.forListeningPort());
		waitingFor(Wait.forLogMessage(".*Successfully started Redpanda.*", 1));
	}

	private void setupContainer(String name, CreateContainerCmd createContainerCmd) {
		createContainerCmd.withHostName(name);
		createContainerCmd.withName(name);
	}

	public int getKafkaPort() {
		return getMappedPort(KAFKA_PORT);
	}

	@Override
	public void start() {
		addFixedExposedPort(KAFKA_PORT, KAFKA_PORT);
		super.start();
	}
}
