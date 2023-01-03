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
package org.apache.camel.component.opensearch.integration;

import org.apache.camel.CamelContext;
import org.apache.camel.component.opensearch.OpensearchComponent;
import org.apache.camel.test.infra.opensearch.services.OpenSearchService;
import org.apache.camel.test.infra.opensearch.services.OpenSearchServiceFactory;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.http.HttpHost;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OpensearchTestSupport extends CamelTestSupport {

	@RegisterExtension
	public static OpenSearchService service = OpenSearchServiceFactory.createService();

	protected static String clusterName = "docker-cluster";
	protected static RestClient restClient;
	protected static RestHighLevelClient client;

	private static final Logger LOG = LoggerFactory.getLogger(OpensearchTestSupport.class);

	@Override
	protected void setupResources() throws Exception {
		super.setupResources();
		HttpHost host
				= new HttpHost(service.getOpenSearchHost(), service.getPort());
		client = new RestHighLevelClient(RestClient.builder(host));
		restClient = client.getLowLevelClient();
	}

	@Override
	protected void cleanupResources() throws Exception {
		super.cleanupResources();
		if (client != null) {
			client.close();
		}
	}

	@Override
	protected CamelContext createCamelContext() throws Exception {
		final OpensearchComponent opensearchComponent = new OpensearchComponent();
		opensearchComponent.setHostAddresses(service.getHttpHostAddress());

		CamelContext context = super.createCamelContext();
		context.addComponent("opensearch-rest", opensearchComponent);

		return context;
	}

	/**
	 * As we don't delete the {@code target/data} folder for <b>each</b> test below (otherwise they would run much
	 * slower), we need to make sure there's no side effect of the same used data through creating unique indexes.
	 */
	Map<String, String> createIndexedData(String... additionalPrefixes) {
		String prefix = createPrefix();

		// take over any potential prefixes we may have been asked for
		if (additionalPrefixes.length > 0) {
			StringBuilder sb = new StringBuilder(prefix);
			for (String additionalPrefix : additionalPrefixes) {
				sb.append(additionalPrefix).append("-");
			}
			prefix = sb.toString();
		}

		String key = prefix + "key";
		String value = prefix + "value";
		LOG.info("Creating indexed data using the key/value pair {} => {}", key, value);

		Map<String, String> map = new HashMap<>();
		map.put(key, value);
		return map;
	}

	String createPrefix() {
		// make use of the test method name to avoid collision
		return getCurrentTestName().toLowerCase() + "-";
	}

	RestClient getClient() {
		return restClient;
	}
}
