package org.apache.camel.test.infra.artemis.services;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreAddressConfiguration;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.camel.test.AvailablePortFinder;

public class ArtemisAMQPService extends AbstractArtemisEmbeddedService {

	private String brokerURL;
	private int amqpPort;

	public ArtemisAMQPService() {
	}

	@Override
	protected Configuration getConfiguration(Configuration artemisConfiguration, int port) {
		amqpPort = port;
		brokerURL = "tcp://0.0.0.0:" + amqpPort
				+ "?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpMinCredits=300";

		AddressSettings addressSettings = new AddressSettings();
		// Disable auto create address to make sure that topic name is correct without prefix
//		addressSettings.setAutoCreateAddresses(false);
		try {
			artemisConfiguration.addAcceptorConfiguration("amqp", brokerURL);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
			fail("AMQP acceptor cannot be configured");
		}
		artemisConfiguration.setPersistenceEnabled(false);
		artemisConfiguration.addAddressesSetting("#", addressSettings);
		artemisConfiguration.setSecurityEnabled(false);

		// Set explicit topic name
		CoreAddressConfiguration pingTopicConfig = new CoreAddressConfiguration();
		pingTopicConfig.setName("topic.ping");
		pingTopicConfig.addRoutingType(RoutingType.MULTICAST);

		artemisConfiguration.addAddressConfiguration(pingTopicConfig);

		return artemisConfiguration;
	}

	@Override
	public String serviceAddress() {
		return brokerURL;
	}

	@Override
	public int brokerPort() {
		return amqpPort;
	}
}
