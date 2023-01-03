package org.apache.camel.test.infra.artemis.services;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;

public class ArtemisVMService extends AbstractArtemisEmbeddedService {

	private String brokerURL;

	@Override
	protected Configuration getConfiguration(Configuration configuration, int port) {
		final int brokerId = super.BROKER_COUNT.intValue();
		brokerURL = "vm://" + brokerId;

		configuration.setPersistenceEnabled(false);
		try {
			configuration.addAcceptorConfiguration("in-vm", "vm://" + brokerId);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
			fail("vm acceptor cannot be configured");
		}
		configuration.addAddressSetting("#",
				new AddressSettings()
						.setDeadLetterAddress(SimpleString.toSimpleString("DLQ"))
						.setExpiryAddress(SimpleString.toSimpleString("ExpiryQueue")));

		return configuration;
	}

	@Override
	public String serviceAddress() {
		return brokerURL;
	}

	@Override
	public int brokerPort() {
		return 0;
	}
}
