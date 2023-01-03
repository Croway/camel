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

import static org.apache.camel.test.junit5.TestSupport.assertCollectionSize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.camel.builder.RouteBuilder;

import org.junit.jupiter.api.Test;

import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.index.IndexRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpensearchBulkIT extends OpensearchTestSupport {

	@Test
	public void testBulkIndex() {
		List<Map<String, String>> documents = new ArrayList<>();
		Map<String, String> document1 = createIndexedData("1");
		Map<String, String> document2 = createIndexedData("2");

		documents.add(document1);
		documents.add(document2);

		List<?> indexIds = template.requestBody("direct:bulk_index", documents, List.class);
		assertNotNull(indexIds, "indexIds should be set");
		assertCollectionSize("Indexed documents should match the size of documents", indexIds, documents.size());
	}

	@Test
	public void bulkIndexListRequestBody() {
		String prefix = createPrefix();

		// given
		List<Map<String, String>> request = new ArrayList<>();
		final HashMap<String, String> valueMap = new HashMap<>();
		valueMap.put("id", prefix + "baz");
		valueMap.put("content", prefix + "hello");
		request.add(valueMap);
		// when
		@SuppressWarnings("unchecked")
		List<String> indexedDocumentIds = template.requestBody("direct:bulk_index", request, List.class);

		// then
		assertThat(indexedDocumentIds, notNullValue());
		assertThat(indexedDocumentIds.size(), equalTo(1));
	}

	@Test
	public void bulkIndexRequestBody() {
		String prefix = createPrefix();

		// given
		BulkRequest request = new BulkRequest();
		request.add(
				new IndexRequest(prefix + "foo").id(prefix + "baz").source(prefix + "content", prefix + "hello"));

		// when
		BulkItemResponse[] response = template.requestBody("direct:bulk_index", request, BulkItemResponse[].class);

		// then
		assertThat(response, notNullValue());
		assertThat(response.length, equalTo(1));
		assertThat(response[0].isFailed(), equalTo(false));
		assertThat(response[0].getId(), equalTo(prefix + "baz"));
	}

	@Test
	public void bulkRequestBody() {
		String prefix = createPrefix();

		// given
		BulkRequest request = new BulkRequest();
		request.add(
				new IndexRequest(prefix + "foo").id(prefix + "baz").source(prefix + "content", prefix + "hello"));

		// when
		BulkItemResponse[] response = (BulkItemResponse[]) template.requestBody("direct:bulk", request);

		// then
		assertThat(response, notNullValue());
		assertEquals(prefix + "baz", response[0].getResponse().getId());
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() {
				from("direct:bulk_index")
						.to("opensearch-rest://opensearch?operation=BulkIndex&indexName=twitter");
				from("direct:bulk")
						.to("opensearch-rest://opensearch?operation=Bulk&indexName=twitter");
			}
		};
	}
}
