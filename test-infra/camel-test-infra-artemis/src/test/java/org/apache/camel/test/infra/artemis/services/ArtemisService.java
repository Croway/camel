package org.apache.camel.test.infra.artemis.services;

import org.apache.camel.test.infra.artemis.common.ArtemisProperties;
import org.apache.camel.test.infra.common.services.TestService;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public interface ArtemisService extends BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, TestService {

	String serviceAddress();

	String userName();

	String password();

	int brokerPort();

	default void registerProperties() {
		// For compatibility with the previous format used by camel-sjms tests
		System.setProperty(ArtemisProperties.SERVICE_ADDRESS, serviceAddress());
		System.setProperty(ArtemisProperties.ARTEMIS_EXTERNAL, serviceAddress());
		System.setProperty(ArtemisProperties.ARTEMIS_USERNAME, userName());
		System.setProperty(ArtemisProperties.ARTEMIS_PASSWORD, userName());
	}

	@Override
	default void beforeAll(ExtensionContext extensionContext) throws Exception {
		initialize();
	}

	@Override
	default void afterAll(ExtensionContext extensionContext) throws Exception {
		shutdown();
	}

	@Override
	default void afterEach(ExtensionContext extensionContext) throws Exception {
		shutdown();
	}

	@Override
	default void beforeEach(ExtensionContext extensionContext) throws Exception {
		initialize();
	}

	void restart();
}
