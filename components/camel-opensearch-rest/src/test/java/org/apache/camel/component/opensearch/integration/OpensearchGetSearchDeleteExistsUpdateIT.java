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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.opensearch.OpensearchConstants;
import org.apache.camel.component.opensearch.OpensearchOperation;

import org.junit.jupiter.api.Test;

import org.opensearch.action.DocWriteResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.MultiSearchRequest;
import org.opensearch.action.search.MultiSearchResponse.Item;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.search.SearchHits;

import java.util.HashMap;
import java.util.Map;

public class OpensearchGetSearchDeleteExistsUpdateIT extends OpensearchTestSupport {

	@Test
	public void testGet() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		sendBody("direct:index", map);
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");

		//now, verify GET succeeded
		GetResponse response = template.requestBody("direct:get", indexId, GetResponse.class);
		assertNotNull(response, "response should not be null");
		assertNotNull(response.getSource(), "response source should not be null");
	}

	@Test
	public void testDelete() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		sendBody("direct:index", map);
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");

		//now, verify GET succeeded
		GetResponse response = template.requestBody("direct:get", indexId, GetResponse.class);
		assertNotNull(response, "response should not be null");
		assertNotNull(response.getSource(), "response source should not be null");

		//now, perform Delete
		DeleteResponse.Result deleteResponse = template.requestBody("direct:delete", indexId, DeleteResponse.Result.class);
		assertNotNull(deleteResponse, "response should not be null");

		//now, verify GET fails to find the indexed value
		response = template.requestBody("direct:get", indexId, GetResponse.class);
		assertNotNull(response, "response should not be null");
		assertNull(response.getSource(), "response source should be null");
	}

	@Test
	public void testSearchWithMapQuery() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");

		//now, verify GET succeeded
		GetResponse getResponse = template.requestBody("direct:get", indexId, GetResponse.class);
		assertNotNull(getResponse, "response should not be null");
		assertNotNull(getResponse.getSource(), "response source should not be null");
		//now, verify GET succeeded
		Map<String, Object> actualQuery = new HashMap<>();
		actualQuery.put("testsearchwithmapquery-key", "testsearchwithmapquery-value");
		Map<String, Object> match = new HashMap<>();
		match.put("match", actualQuery);
		Map<String, Object> query = new HashMap<>();
		query.put("query", match);
		SearchHits response = template.requestBody("direct:search", query, SearchHits.class);
		assertNotNull(response, "response should not be null");
		assertEquals(0, response.getTotalHits().value, "response hits should be == 0");
	}

	@Test
	public void testSearchWithStringQuery() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");

		//now, verify GET succeeded
		GetResponse getResponse = template.requestBody("direct:get", indexId, GetResponse.class);
		assertNotNull(getResponse, "response should not be null");
		assertNotNull(getResponse.getSource(), "response source should not be null");
		// need to create a query string
		String query = "{\n"
				+ "    \"query\" : { \"match\" : { \"key\" : \"value\" }}\n"
				+ "}\n";
		SearchHits response = template.requestBody("direct:search", query, SearchHits.class);
		assertNotNull(response, "response should not be null");
		assertEquals(0, response.getTotalHits().value, "response hits should be == 0");

		// testing

		response = template.requestBody("direct:search-1", query, SearchHits.class);
		assertNotNull(response, "response should not be null");
		assertEquals(0, response.getTotalHits().value, "response hits should be == 0");
	}

	@Test
	public void testMultiSearch() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");

		//now, verify GET succeeded
		GetResponse getResponse = template.requestBody("direct:get", indexId, GetResponse.class);
		assertNotNull(getResponse, "response should not be null");
		assertNotNull(getResponse.getSource(), "response source should not be null");
		//now, verify GET succeeded
		SearchRequest req = new SearchRequest();
		req.indices("twitter");
		SearchRequest req1 = new SearchRequest();
		req.indices("twitter");
		MultiSearchRequest request = new MultiSearchRequest().add(req1).add(req);
		Item[] response = template.requestBody("direct:search", request, Item[].class);
		assertNotNull(response, "response should not be null");
		assertEquals(2, response.length, "response should be == 2");
	}

	@Test
	public void testUpdate() {
		Map<String, String> map = createIndexedData();
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");

		Map<String, String> newMap = new HashMap<>();
		newMap.put(createPrefix() + "key2", createPrefix() + "value2");
		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_INDEX_ID, indexId);
		indexId = template.requestBodyAndHeaders("direct:update", newMap, headers, String.class);
		assertNotNull(indexId, "indexId should be set");
	}

	@Test
	public void testGetWithHeaders() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Index);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter");

		String indexId = template.requestBodyAndHeaders("direct:start", map, headers, String.class);

		//now, verify GET
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.GetById);
		GetResponse response = template.requestBodyAndHeaders("direct:start", indexId, headers, GetResponse.class);
		assertNotNull(response, "response should not be null");
		assertNotNull(response.getSource(), "response source should not be null");
	}

	@Test
	public void testExistsWithHeaders() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Index);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter");

		template.requestBodyAndHeaders("direct:start", map, headers, String.class);

		//now, verify GET
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Exists);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter");
		Boolean exists = template.requestBodyAndHeaders("direct:exists", "", headers, Boolean.class);
		assertNotNull(exists, "response should not be null");
		assertTrue(exists, "Index should exists");
	}

	@Test
	public void testNotExistsWithHeaders() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Index);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter");

		template.requestBodyAndHeaders("direct:start", map, headers, String.class);

		//now, verify GET
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Exists);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter-tweet");
		Boolean exists = template.requestBodyAndHeaders("direct:exists", "", headers, Boolean.class);
		assertNotNull(exists, "response should not be null");
		assertFalse(exists, "Index should not exists");
	}

	@Test
	public void testDeleteWithHeaders() {
		//first, Index a value
		Map<String, String> map = createIndexedData();
		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Index);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter");

		String indexId = template.requestBodyAndHeaders("direct:start", map, headers, String.class);

		//now, verify GET
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.GetById);
		GetResponse response = template.requestBodyAndHeaders("direct:start", indexId, headers, GetResponse.class);
		assertNotNull(response, "response should not be null");
		assertNotNull(response.getSource(), "response source should not be null");

		//now, perform Delete
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Delete);
		DocWriteResponse.Result deleteResponse
				= template.requestBodyAndHeaders("direct:start", indexId, headers, DocWriteResponse.Result.class);
		assertEquals(DocWriteResponse.Result.DELETED, deleteResponse, "response should not be null");

		//now, verify GET fails to find the indexed value
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.GetById);
		response = template.requestBodyAndHeaders("direct:start", indexId, headers, GetResponse.class);
		assertNotNull(response, "response should not be null");
		assertNull(response.getSource(), "response source should be null");
	}

	@Test
	public void testUpdateWithIDInHeader() {
		Map<String, String> map = createIndexedData();
		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Index);
		headers.put(OpensearchConstants.PARAM_INDEX_NAME, "twitter");
		headers.put(OpensearchConstants.PARAM_INDEX_ID, "123");

		String indexId = template.requestBodyAndHeaders("direct:start", map, headers, String.class);
		assertNotNull(indexId, "indexId should be set");
		assertEquals("123", indexId, "indexId should be equals to the provided id");

		headers.put(OpensearchConstants.PARAM_OPERATION, OpensearchOperation.Update);

		indexId = template.requestBodyAndHeaders("direct:start", map, headers, String.class);
		assertNotNull(indexId, "indexId should be set");
		assertEquals("123", indexId, "indexId should be equals to the provided id");
	}

	@Test
	public void getRequestBody() {
		String prefix = createPrefix();

		// given
		GetRequest request = new GetRequest(prefix + "foo");

		// when
		String documentId = template.requestBody("direct:index",
				new IndexRequest(prefix + "foo").id(prefix + "testId")
						.source(prefix + "content", prefix + "hello"),
				String.class);
		GetResponse response = template.requestBody("direct:get",
				request.id(documentId), GetResponse.class);

		// then
		assertThat(response, notNullValue());

		assertThat(prefix + "hello", equalTo(response.getSourceAsMap().get(prefix + "content")));
	}

	@Test
	public void deleteRequestBody() {
		String prefix = createPrefix();

		// given
		DeleteRequest request = new DeleteRequest(prefix + "foo");

		// when
		String documentId = template.requestBody("direct:index",
				new IndexRequest("" + prefix + "foo").id("" + prefix + "testId")
						.source(prefix + "content", prefix + "hello"),
				String.class);
		DeleteResponse.Result response
				= template.requestBody("direct:delete", request.id(documentId), DeleteResponse.Result.class);

		// then
		assertThat(response, notNullValue());
	}

	@Test
	public void testStringUpdate() {
		Map<String, String> map = createIndexedData();
		String indexId = template.requestBody("direct:index", map, String.class);
		assertNotNull(indexId, "indexId should be set");

		String body = "{\"teststringupdate-key\" : \"teststringupdate-updated\"}";

		Map<String, Object> headers = new HashMap<>();
		headers.put(OpensearchConstants.PARAM_INDEX_ID, indexId);
		indexId = template.requestBodyAndHeaders("direct:update", body, headers, String.class);
		assertNotNull(indexId, "indexId should be set");

		GetResponse response = template.requestBody("direct:get", indexId, GetResponse.class);
		assertEquals("teststringupdate-updated", response.getSource().get("teststringupdate-key"));
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() {
				from("direct:start")
						.to("opensearch-rest://opensearch?operation=Index");
				from("direct:index")
						.to("opensearch-rest://opensearch?operation=Index&indexName=twitter");
				from("direct:get")
						.to("opensearch-rest://opensearch?operation=GetById&indexName=twitter");
				from("direct:multiget")
						.to("opensearch-rest://opensearch?operation=MultiGet&indexName=twitter");
				from("direct:delete")
						.to("opensearch-rest://opensearch?operation=Delete&indexName=twitter");
				from("direct:search")
						.to("opensearch-rest://opensearch?operation=Search&indexName=twitter");
				from("direct:search-1")
						.to("opensearch-rest://opensearch?operation=Search");
				from("direct:multiSearch")
						.to("opensearch-rest://opensearch?operation=MultiSearch");
				from("direct:update")
						.to("opensearch-rest://opensearch?operation=Update&indexName=twitter");
				from("direct:exists")
						.to("opensearch-rest://opensearch?operation=Exists");
			}
		};
	}
}
