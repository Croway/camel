package org.apache.camel.test.infra.artemis.common;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.test.infra.artemis.services.AbstractArtemisEmbeddedService;
import org.apache.camel.test.infra.artemis.services.ArtemisService;
import org.apache.camel.test.infra.artemis.services.ArtemisServiceFactory;
import org.apache.camel.test.infra.messaging.services.ConnectionFactoryAware;

import javax.jms.ConnectionFactory;

public class ConnectionFactoryHelper {
	private ConnectionFactoryHelper() {
	}

	public static ConnectionFactory createConnectionFactory(ArtemisService service) {
		return createConnectionFactory(service, null);
	}

	public static ConnectionFactory createConnectionFactory(ArtemisService service, Integer maximumRedeliveries) {
		if (service instanceof ConnectionFactoryAware) {
			AbstractArtemisEmbeddedService embeddedService = (AbstractArtemisEmbeddedService) service;

			return createConnectionFactory(embeddedService.serviceAddress(), maximumRedeliveries);
		}

		if (service instanceof ArtemisServiceFactory.SingletonArtemisService) {
			return createConnectionFactory(((ArtemisServiceFactory.SingletonArtemisService) service).getService(), maximumRedeliveries);
		}

		throw new UnsupportedOperationException(String.format("The test service %s does not implement ConnectionFactoryAware", service.getClass()));
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
