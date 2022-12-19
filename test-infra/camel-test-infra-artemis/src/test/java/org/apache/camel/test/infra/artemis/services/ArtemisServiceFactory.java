package org.apache.camel.test.infra.artemis.services;

public class ArtemisServiceFactory {

	private static ArtemisService persistentService;
	private static ArtemisService nonPersistentService;
	private static ArtemisService amqpService;

	public static synchronized ArtemisService createVMService() {
		if (persistentService == null) {
			persistentService = new ArtemisVMService();
		}

		return persistentService;
	}

	public static synchronized ArtemisService createPersistentVMService() {
		if (nonPersistentService == null) {
			nonPersistentService = new ArtemisPersistentVMService();
		}

		return nonPersistentService;
	}

	public static ArtemisService createAMQPService() {
		return new ArtemisAMQPService();
	}
}
