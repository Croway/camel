package org.apache.camel.test.infra.artemis.services;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;

public class ArtemisPersistentVMService extends AbstractArtemisEmbeddedService {

	private String brokerURL;

	@Override
	protected Configuration getConfiguration(Configuration configuration, int port) {
		brokerURL = "vm://0";

		configuration.setPersistenceEnabled(true);
		try {
			configuration.addAcceptorConfiguration("in-vm", brokerURL);
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
