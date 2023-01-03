/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.test.infra.opensearch.services;

import org.apache.camel.test.infra.common.services.ContainerService;
import org.apache.camel.test.infra.opensearch.common.OpenSearchProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class OpenSearchLocalContainerService implements OpenSearchService, ContainerService<GenericContainer> {
	public static final String DEFAULT_OPENSEARCH_CONTAINER = "opensearchproject/opensearch:2.4.1";
	public static final String CONTAINER_NAME = "opensearch";

	private static final Logger LOG = LoggerFactory.getLogger(OpenSearchLocalContainerService.class);
	private static final int OPENSEARCH_PORT = 9200;

	private final GenericContainer container;

	public OpenSearchLocalContainerService() {
		this(System.getProperty(OpenSearchProperties.OPENSEARCH_CONTAINER, DEFAULT_OPENSEARCH_CONTAINER));
	}

	public OpenSearchLocalContainerService(String imageName) {
		container = initContainer(imageName);
	}

	public OpenSearchLocalContainerService(GenericContainer container) {
		this.container = container;
	}

	protected GenericContainer initContainer(String imageName) {
		return initContainer(imageName, CONTAINER_NAME);
	}

	protected GenericContainer initContainer(String imageName, String containerName) {
		return new GenericContainer<>(DockerImageName.parse(imageName))
				.withNetworkAliases(containerName)
				.withExposedPorts(OPENSEARCH_PORT)
				.withEnv("discovery.type", "single-node")
				.withEnv("DISABLE_SECURITY_PLUGIN", "true")
				.withEnv("DISABLE_INSTALL_DEMO_CONFIG", "true")
				.withEnv("DISABLE_PERFORMANCE_ANALYZER_AGENT_CLI", "true")
				.waitingFor(Wait.forListeningPort());
	}

	@Override
	public int getPort() {
		return container.getMappedPort(OPENSEARCH_PORT);
	}

	@Override
	public String getOpenSearchHost() {
		return container.getHost();
	}

	@Override
	public String getHttpHostAddress() {
		return container.getHost() + ":" + getPort();
	}

	@Override
	public void registerProperties() {
		System.setProperty(OpenSearchProperties.OPENSEARCH_HOST, getOpenSearchHost());
		System.setProperty(OpenSearchProperties.OPENSEARCH_PORT, String.valueOf(getPort()));
	}

	@Override
	public void initialize() {
		LOG.info("Trying to start the Opensearch container");
		container.start();

		registerProperties();
		LOG.info("OpenSearch instance running at {}", getHttpHostAddress());
	}

	@Override
	public void shutdown() {
		LOG.info("Stopping the Opensearch container");
		container.stop();
	}

	@Override
	public GenericContainer getContainer() {
		return container;
	}
}
