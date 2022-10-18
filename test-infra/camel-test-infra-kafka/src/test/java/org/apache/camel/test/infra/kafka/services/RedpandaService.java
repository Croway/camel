package org.apache.camel.test.infra.kafka.services;

import org.apache.camel.test.infra.common.TestUtils;
import org.apache.camel.test.infra.common.services.ContainerService;
import org.apache.camel.test.infra.kafka.common.KafkaProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;

public class RedpandaService implements KafkaService, ContainerService<RedpandaContainer> {
	private static final Logger LOG = LoggerFactory.getLogger(RedpandaService.class);

	private final RedpandaContainer redpandaContainer;

	public RedpandaService() {
		this("strimzi-" + TestUtils.randomWithRange(1, 100));
	}

	public RedpandaService(String strimziInstanceName) {
		Network network = Network.newNetwork();

		redpandaContainer = initRedpandaContainer(network, strimziInstanceName);
	}

	public RedpandaService(RedpandaContainer redpandaContainer) {
		this.redpandaContainer = redpandaContainer;
	}

	protected RedpandaContainer initRedpandaContainer(Network network, String instanceName) {
		return new RedpandaContainer(network, instanceName);
	}

	protected Integer getKafkaPort() {
		return redpandaContainer.getKafkaPort();
	}

	@Override
	public String getBootstrapServers() {
		return redpandaContainer.getHost() + ":" + getKafkaPort();
	}

	@Override
	public void registerProperties() {
		System.setProperty(KafkaProperties.KAFKA_BOOTSTRAP_SERVERS, getBootstrapServers());
	}

	@Override
	public void initialize() {
		redpandaContainer.start();

		registerProperties();
		LOG.info("Kafka bootstrap server running at address {}", getBootstrapServers());
	}

	private boolean stopped() {
		return !redpandaContainer.isRunning();
	}

	@Override
	public void shutdown() {
		try {
			LOG.info("Stopping Kafka container");
			redpandaContainer.stop();
		} finally {
			LOG.info("Stopping Zookeeper container");
			redpandaContainer.stop();

			TestUtils.waitFor(this::stopped);
		}
	}

	@Override
	public RedpandaContainer getContainer() {
		return redpandaContainer;
	}
}
