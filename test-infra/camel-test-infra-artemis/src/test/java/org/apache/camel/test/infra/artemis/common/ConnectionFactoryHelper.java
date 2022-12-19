package org.apache.camel.test.infra.artemis.common;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.test.infra.artemis.services.AbstractArtemisEmbeddedService;
import org.apache.camel.test.infra.artemis.services.ArtemisService;

import javax.jms.ConnectionFactory;

public class ConnectionFactoryHelper {
	private ConnectionFactoryHelper() {
	}

	public static ConnectionFactory createConnectionFactory(ArtemisService service) {
		return createConnectionFactory(service, null);
	}

	public static ConnectionFactory createConnectionFactory(ArtemisService service, Integer maximumRedeliveries) {
		if (service instanceof AbstractArtemisEmbeddedService) {
			AbstractArtemisEmbeddedService embeddedService = (AbstractArtemisEmbeddedService) service;

			return new ActiveMQConnectionFactory(embeddedService.serviceAddress());
		}

		throw new UnsupportedOperationException("The test service does not implement ConnectionFactoryAware");
	}

	public static ConnectionFactory createConnectionFactory(String url, Integer maximumRedeliveries) {
		return createConnectionFactory(new ActiveMQConnectionFactory(url), maximumRedeliveries);
	}

	public static ConnectionFactory createConnectionFactory(
			ActiveMQConnectionFactory connectionFactory, Integer maximumRedeliveries) {
		return connectionFactory;
	}

	public static ConnectionFactory createPersistentConnectionFactory(String url) {
		return createPersistentConnectionFactory(new ActiveMQConnectionFactory(url));
	}

	public static ConnectionFactory createPersistentConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
//		connectionFactory.setCopyMessageOnSend(false);
//		connectionFactory.setOptimizeAcknowledge(true);
//		connectionFactory.setOptimizedMessageDispatch(true);
//		connectionFactory.setAlwaysSessionAsync(false);
//		connectionFactory.setTrustAllPackages(true);
		return connectionFactory;
	}
}
