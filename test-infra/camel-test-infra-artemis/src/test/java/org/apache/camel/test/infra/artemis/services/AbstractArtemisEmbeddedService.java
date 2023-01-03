package org.apache.camel.test.infra.artemis.services;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreAddressConfiguration;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.camel.test.AvailablePortFinder;

import org.apache.camel.test.infra.artemis.common.ConnectionFactoryHelper;
import org.apache.camel.test.infra.messaging.services.ConnectionFactoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.jms.ConnectionFactory;

public abstract class AbstractArtemisEmbeddedService implements ArtemisService, ConnectionFactoryAware {

	protected int tcpPort = AvailablePortFinder.getNextAvailable();

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractArtemisEmbeddedService.class);

	protected EmbeddedActiveMQ embeddedBrokerService;

	private Configuration artemisConfiguration;

	public AbstractArtemisEmbeddedService() {
		embeddedBrokerService = new EmbeddedActiveMQ();

		// Base configuration
		artemisConfiguration = new ConfigurationImpl();
		artemisConfiguration.setSecurityEnabled(false);
		artemisConfiguration.setBrokerInstance(new File("target/artemis"));
		artemisConfiguration.setJMXManagementEnabled(false);

		embeddedBrokerService.setConfiguration(getConfiguration(artemisConfiguration, AvailablePortFinder.getNextAvailable()));
	}

	protected abstract Configuration getConfiguration(Configuration artemisConfiguration, int port);

	@Override
	public String userName() {
		return null;
	}

	@Override
	public String password() {
		return null;
	}

	@Override
	public void restart() {

	}

	@Override
	public void initialize() {
		try {
			embeddedBrokerService.start();
			embeddedBrokerService.getActiveMQServer().waitForActivation(20, TimeUnit.SECONDS);
		} catch (Exception e) {
			LOG.warn("Unable to start embedded Artemis broker: {}", e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	@Override
	public void shutdown() {
		try {
			embeddedBrokerService.stop();
		} catch (Exception e) {
			LOG.warn("Unable to start embedded Artemis broker: {}", e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	public EmbeddedActiveMQ getEmbeddedBrokerService() {
		return embeddedBrokerService;
	}

	@Override
	public ConnectionFactory createConnectionFactory() {
		return ConnectionFactoryHelper.createConnectionFactory(this);
	}

	@Override
	public ConnectionFactory createConnectionFactory(Integer maximumRedeliveries) {
		return ConnectionFactoryHelper.createConnectionFactory(this, maximumRedeliveries);
	}
}
