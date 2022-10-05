package org.apache.camel.component.validator;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

import org.junit.jupiter.api.Test;

public class ValidatorWithSchedulerTest extends ContextTestSupport {

	private MockEndpoint valid;
	private MockEndpoint fail;

	@Test
	public void testValidatorWithScheduler() throws Exception {
		valid = resolveMandatoryEndpoint("mock:valid", MockEndpoint.class);
		fail = resolveMandatoryEndpoint("mock:fail", MockEndpoint.class);
		fail.expectedMessageCount(1);
		valid.expectedMessageCount(0);

		valid.assertIsSatisfied();
		fail.assertIsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				final String wrongBody = "<user2><name>Federico</name><surname>Mariani</surname></user2>";

				onException(Exception.class)
						.to("mock:fail");

				validator()
						.type("xml:schemaValidator")
						.withUri("validator:org/apache/camel/impl/validate.xsd?failOnNullBody=false");

				from("scheduler://foo?repeatCount=1")
						.process(exchange ->
								exchange.getIn().setBody(wrongBody))
						.inputTypeWithValidate("xml:schemaValidator")
						.to("mock:valid");
			}
		};
	}
}
