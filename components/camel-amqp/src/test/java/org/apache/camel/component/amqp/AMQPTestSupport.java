package org.apache.camel.component.amqp;

import org.apache.camel.test.infra.artemis.services.ArtemisService;
import org.apache.camel.test.infra.artemis.services.ArtemisServiceFactory;
import org.apache.camel.test.junit5.CamelTestSupport;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

public class AMQPTestSupport extends CamelTestSupport {

	@RegisterExtension
	protected static ArtemisService service = ArtemisServiceFactory.createAMQPService();

	@BeforeAll
	public static void beforeAll() {
		System.setProperty(AMQPConnectionDetails.AMQP_PORT, service.brokerPort() + "");
	}
}
