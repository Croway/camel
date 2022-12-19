package org.apache.camel.test.infra.artemis.services;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.camel.test.AvailablePortFinder;

public class ArtemisTCPService extends AbstractArtemisEmbeddedService {

	private String brokerUrl;
	private int tcpPort;

	public ArtemisTCPService() {
	}

	@Override
	protected Configuration getConfiguration(Configuration configuration, int port) {
		this.tcpPort = port;
		brokerUrl = "tcp://0.0.0.0:" + port + "?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=CORE,AMQP,STOMP,HORNETQ,MQTT,OPENWIRE";
		configuration.setPersistenceEnabled(false);
		try {
			configuration.addAcceptorConfiguration("artemis", brokerUrl);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
			fail("Artemis/TCP acceptor cannot be configured");
		}
		configuration.addAddressSetting("#",
				new AddressSettings()
						.setDeadLetterAddress(SimpleString.toSimpleString("DLQ"))
						.setExpiryAddress(SimpleString.toSimpleString("ExpiryQueue")));

		return configuration;
	}

	@Override
	public String serviceAddress() {
		return brokerUrl;
	}

	@Override
	public int brokerPort() {
		return tcpPort;
	}
}
