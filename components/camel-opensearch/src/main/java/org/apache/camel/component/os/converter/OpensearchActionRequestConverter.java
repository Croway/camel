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
package org.apache.camel.component.os.converter;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.component.os.OpensearchConstants;
import org.apache.camel.util.ObjectHelper;

import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.JsonpDeserializer;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.WaitForActiveShards;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.MgetRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.CreateOperation;
import org.opensearch.client.opensearch.core.bulk.DeleteOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.core.bulk.UpdateOperation;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;

@Converter(generateLoader = true)
public final class OpensearchActionRequestConverter {
	private static final Logger LOG = LoggerFactory.getLogger(OpensearchActionRequestConverter.class);

	private static final String ES_QUERY_DSL_PREFIX = "query";

	private OpensearchActionRequestConverter() {
	}

	// Index requests
	private static IndexOperation.Builder<?> createIndexOperationBuilder(Object document, Exchange exchange)
			throws IOException {
		if (document instanceof IndexOperation.Builder) {
			return (IndexOperation.Builder<?>) document;
		}
		IndexOperation.Builder<Object> builder = new IndexOperation.Builder<>();
		if (document instanceof byte[]) {
			builder.document(JsonData.from(Json.createParser(new ByteArrayInputStream((byte[]) document)), new JacksonJsonpMapper()));
		} else if (document instanceof InputStream) {
			builder.document(JsonData.from(Json.createParser((InputStream) document), new JacksonJsonpMapper()));
		} else if (document instanceof String) {
			builder.document(JsonData.from(Json.createParser(new StringReader((String) document)), new JacksonJsonpMapper()));
		} else if (document instanceof Reader) {
			builder.document(JsonData.from(Json.createParser((Reader) document), new JacksonJsonpMapper()));
		} else if (document instanceof Map) {
			ObjectMapper objectMapper = new ObjectMapper();
			builder.document(JsonData.from(Json.createParser(new StringReader(objectMapper.writeValueAsString(document))), new JacksonJsonpMapper()));
		} else {
			builder.document(document);
		}
		return builder
				.index(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class));
	}

	@Converter
	public static IndexRequest.Builder<?> toIndexRequestBuilder(Object document, Exchange exchange) throws IOException {
		if (document instanceof IndexRequest.Builder) {
			IndexRequest.Builder<?> builder = (IndexRequest.Builder<?>) document;
			return builder.id(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_ID, String.class));
		}
		IndexRequest.Builder<Object> builder = new IndexRequest.Builder<>();

		JsonpDeserializer<JsonData> tDocumentDeserializer = JsonData._DESERIALIZER;


		if (document instanceof byte[]) {
			builder.document(tDocumentDeserializer.deserialize(Json.createParser(new ByteArrayInputStream((byte[]) document)), new JacksonJsonpMapper()));
		} else if (document instanceof InputStream) {
			builder.document(tDocumentDeserializer.deserialize(Json.createParser((InputStream) document), new JacksonJsonpMapper()));
		} else if (document instanceof String) {
			builder.document(tDocumentDeserializer.deserialize(Json.createParser(new StringReader((String) document)), new JacksonJsonpMapper()));
		} else if (document instanceof Reader) {
			builder.document(tDocumentDeserializer.deserialize(Json.createParser((Reader) document), new JacksonJsonpMapper()));
		} else if (document instanceof Map) {
			ObjectMapper objectMapper = new ObjectMapper();
			builder.document(tDocumentDeserializer.deserialize(Json.createParser(new StringReader(objectMapper.writeValueAsString(document))), new JacksonJsonpMapper()));
		} else {
			builder.document(document);
		}
		return builder
				.waitForActiveShards(
						new WaitForActiveShards.Builder()
								.count(exchange.getIn().getHeader(OpensearchConstants.PARAM_WAIT_FOR_ACTIVE_SHARDS,
										Integer.class))
								.build())
				.id(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_ID, String.class))
				.index(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class));
	}

	@Converter
	public static UpdateRequest.Builder<?, ?> toUpdateRequestBuilder(Object document, Exchange exchange) throws IOException {
		if (document instanceof UpdateRequest.Builder) {
			UpdateRequest.Builder<?, ?> builder = (UpdateRequest.Builder<?, ?>) document;
			return builder.id(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_ID, String.class));
		}
		UpdateRequest.Builder<?, Object> builder = new UpdateRequest.Builder<>();
//		if (document instanceof byte[]) {
//			builder.withJson(new ByteArrayInputStream((byte[]) document));
//		} else if (document instanceof InputStream) {
//			builder.withJson((InputStream) document);
//		} else if (document instanceof String) {
//			builder.withJson(new StringReader((String) document));
//		} else if (document instanceof Reader) {
//			builder.withJson((Reader) document);
//		} else if (document instanceof Map) {
//			ObjectMapper objectMapper = new ObjectMapper();
//			builder.withJson(new StringReader(objectMapper.writeValueAsString(document)));
//		} else {
//			builder.doc(document);
//		}

		return builder
				.waitForActiveShards(
						new WaitForActiveShards.Builder()
								.count(exchange.getIn().getHeader(OpensearchConstants.PARAM_WAIT_FOR_ACTIVE_SHARDS,
										Integer.class))
								.build())
				.index(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class))
				.id(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_ID, String.class));
	}

	@Converter
	public static GetRequest.Builder toGetRequestBuilder(Object document, Exchange exchange) {
		if (document instanceof GetRequest.Builder) {
			return (GetRequest.Builder) document;
		}
		if (document instanceof String) {
			return new GetRequest.Builder()
					.index(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class))
					.id((String) document);
		}
		return null;
	}

	@Converter
	public static DeleteRequest.Builder toDeleteRequestBuilder(Object document, Exchange exchange) {
		if (document instanceof DeleteRequest.Builder) {
			return (DeleteRequest.Builder) document;
		}
		if (document instanceof String) {
			return new DeleteRequest.Builder()
					.index(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class))
					.id((String) document);
		}
		return null;
	}

	@Converter
	public static DeleteIndexRequest.Builder toDeleteIndexRequestBuilder(Object document, Exchange exchange) {
		if (document instanceof DeleteIndexRequest.Builder) {
			return (DeleteIndexRequest.Builder) document;
		}
		if (document instanceof String) {
			return new DeleteIndexRequest.Builder()
					.index(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class));
		}
		return null;
	}

	@Converter
	public static MgetRequest.Builder toMgetRequestBuilder(Object documents, Exchange exchange) {
		if (documents instanceof MgetRequest.Builder) {
			return (MgetRequest.Builder) documents;
		}
		if (documents instanceof Iterable) {
			MgetRequest.Builder builder = new MgetRequest.Builder();
			builder.index(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class));
			for (Object document : (List<?>) documents) {
				if (document instanceof String) {
					builder.ids((String) document);
				} else {
					LOG.warn(
							"Cannot convert document id of type {} into a String",
							document == null ? "null" : document.getClass().getName());
					return null;
				}
			}
			return builder;
		}
		return null;
	}

	@Converter
	public static SearchRequest.Builder toSearchRequestBuilder(Object queryObject, Exchange exchange) throws IOException {
		String indexName = exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class);

		if (queryObject instanceof SearchRequest.Builder) {
			SearchRequest.Builder builder = (SearchRequest.Builder) queryObject;
			if (builder.build().index().isEmpty()) {
				builder.index(indexName);
			}
			return builder;
		}
		SearchRequest.Builder builder = new SearchRequest.Builder();

		// Only setup the indexName if the message header has the
		// setting

		Integer size = exchange.getIn().getHeader(OpensearchConstants.PARAM_SIZE, Integer.class);
		Integer from = exchange.getIn().getHeader(OpensearchConstants.PARAM_FROM, Integer.class);
		if (ObjectHelper.isNotEmpty(indexName)) {
			builder.index(indexName);
		}

		String queryText;

		if (queryObject instanceof Map<?, ?>) {
			Map<?, ?> mapQuery = (Map<?, ?>) queryObject;
			// Remove 'query' prefix from the query object for backward
			// compatibility
			if (mapQuery.containsKey(ES_QUERY_DSL_PREFIX)) {
				mapQuery = (Map<?, ?>) mapQuery.get(ES_QUERY_DSL_PREFIX);
			}
			ObjectMapper objectMapper = new ObjectMapper();
			queryText = objectMapper.writeValueAsString(mapQuery);
		} else if (queryObject instanceof String) {
			queryText = (String) queryObject;
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonTextObject = mapper.readValue(queryText, JsonNode.class);
			JsonNode parentJsonNode = jsonTextObject.get(ES_QUERY_DSL_PREFIX);
			if (parentJsonNode != null) {
				queryText = parentJsonNode.toString();
			}
		} else {
			// Cannot convert the queryObject into SearchRequest
			LOG.warn(
					"Cannot convert queryObject of type {} into SearchRequest object",
					queryObject == null ? "null" : queryObject.getClass().getName());
			return null;
		}
		if (size != null) {
			builder.size(size);
		}
		if (from != null) {
			builder.from(from);
		}
//		builder.query(new Query.Builder().withJson(new StringReader(queryText)).build());

		return builder;
	}

	@Converter
	public static BulkRequest.Builder toBulkRequestBuilder(Object documents, Exchange exchange) throws IOException {
		if (documents instanceof BulkRequest.Builder) {
			return (BulkRequest.Builder) documents;
		}
		if (documents instanceof Iterable) {
			BulkRequest.Builder builder = new BulkRequest.Builder();
			builder.index(exchange.getIn().getHeader(OpensearchConstants.PARAM_INDEX_NAME, String.class));
			for (Object document : (List<?>) documents) {
				if (document instanceof DeleteOperation.Builder) {
					builder.operations(
							new BulkOperation.Builder().delete(((DeleteOperation.Builder) document).build()).build());
				} else if (document instanceof UpdateOperation.Builder) {
					builder.operations(
							new BulkOperation.Builder().update(((UpdateOperation.Builder<?>) document).build()).build());
				} else if (document instanceof CreateOperation.Builder) {
					builder.operations(
							new BulkOperation.Builder().create(((CreateOperation.Builder<?>) document).build()).build());
				} else {
					builder.operations(
							new BulkOperation.Builder().index(createIndexOperationBuilder(document, exchange).build()).build());
				}
			}
			return builder;
		}
		return null;
	}
}
