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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.opensearch.OpensearchConstants;
import org.apache.camel.component.opensearch.OpensearchOperation;

import org.junit.jupiter.api.Test;

import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;

import java.util.HashMap;
import java.util.Map;

public class OpensearchIndexIT extends OpensearchTestSupport {

	@Test
	public void testIndex() {
		Map<String, String> map = createIndexedData();
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");
	}

	@Test
	public void testIndexDelete() {
		Map<String, String> map = createIndexedData();
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");

		DeleteIndexRequest index = new DeleteIndexRequest("_all");
		Boolean status = template.requestBody("direct:deleteIndex", index, Boolean.class);
		assertEquals(true, status, "status should be 200");
	}

	@Test
	public void testIndexWithReplication() {
		Map<String, String> map = createIndexedData();
		String indexId = template.requestBody("direct:indexWithReplication", map, String.class);
		assertNotNull(indexId, "indexId should be set");
	}

	@Test
	public void testIndexWithHeaders() {
		Map<String, String> map = createIndexedData();
		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Index);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter");

		String indexId = template.requestBodyAndHeaders("direct:start", map, headers, String.class);
		assertNotNull(indexId, "indexId should be set");
	}

	@Test
	public void testIndexWithIDInHeader() {
		Map<String, String> map = createIndexedData();
		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Index);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter");
		headers.put(OpensearchConstants.PARAM_INDEX_ID, "123");

		String indexId = template.requestBodyAndHeaders("direct:start", map, headers, String.class);
		assertNotNull(indexId, "indexId should be set");
		assertEquals("123", indexId, "indexId should be equals to the provided id");
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() {
				from("direct:start")
						.to("opensearch-rest://opensearch");
				from("direct:index")
						.to("opensearch-rest://opensearch?operation=Index&indexName=twitter");
				from("direct:deleteIndex")
						.to("opensearch-rest://opensearch?operation=DeleteIndex&indexName=twitter");
				from("direct:indexWithReplication")
						.to("opensearch-rest://opensearch?operation=Index&indexName=twitter");
			}
		};
	}
}
