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
package org.apache.camel.component.javahttp;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpDynamicUriTest extends JavaHttpTestBase {

    @Test
    public void testDynamicUri() throws Exception {
        // Setup WireMock stub for dynamic URI
        wireMock.stubFor(get(urlEqualTo("/status/201"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withBody("Status 201")));

        Exchange exchange = template.request("direct:dynamicUri", exchange1 -> {
            exchange1.getIn().setHeader(JavaHttpConstants.HTTP_URI, getWireMockBaseUrl() + "/status/201");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(201, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testDynamicPath() throws Exception {
        // Setup WireMock stub for dynamic path
        wireMock.stubFor(get(urlEqualTo("/status/202"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withBody("Status 202")));

        Exchange exchange = template.request("direct:dynamicPath", exchange1 -> {
            exchange1.getIn().setHeader(JavaHttpConstants.HTTP_PATH, "/status/202");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(202, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testDynamicQuery() throws Exception {
        // Setup WireMock stub for dynamic query
        wireMock.stubFor(get(urlPathEqualTo("/get"))
                .withQueryParam("foo", equalTo("bar"))
                .withQueryParam("baz", equalTo("qux"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"args\":{\"foo\":\"bar\",\"baz\":\"qux\"}}")));

        Exchange exchange = template.request("direct:dynamicQuery", exchange1 -> {
            exchange1.getIn().setHeader(JavaHttpConstants.HTTP_QUERY, "foo=bar&baz=qux");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        // The response should contain our query parameters
        String response = message.getBody(String.class);
        assertNotNull(response);
    }

    @Test
    public void testDynamicPathAndQuery() throws Exception {
        // Setup WireMock stub for dynamic path and query
        wireMock.stubFor(get(urlPathEqualTo("/get"))
                .withQueryParam("test", equalTo("dynamic"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"args\":{\"test\":\"dynamic\"}}")));

        Exchange exchange = template.request("direct:dynamicPathAndQuery", exchange1 -> {
            exchange1.getIn().setHeader(JavaHttpConstants.HTTP_PATH, "/get");
            exchange1.getIn().setHeader(JavaHttpConstants.HTTP_QUERY, "test=dynamic");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testMethodOverride() throws Exception {
        // Setup WireMock stub for POST method override
        wireMock.stubFor(post(urlEqualTo("/post"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson("{\"dynamic\":\"method\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"json\":{\"dynamic\":\"method\"}}")));

        Exchange exchange = template.request("direct:methodOverride", exchange1 -> {
            exchange1.getIn().setHeader(JavaHttpConstants.HTTP_METHOD, "POST");
            exchange1.getIn().setBody("{\"dynamic\":\"method\"}");
            exchange1.getIn().setHeader("Content-Type", "application/json");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:dynamicUri")
                        .to("java-http://localhost:" + getWireMockPort() + "/get?httpMethod=GET");

                from("direct:dynamicPath")
                        .to("java-http://localhost:" + getWireMockPort() + "?httpMethod=GET");

                from("direct:dynamicQuery")
                        .to("java-http://localhost:" + getWireMockPort() + "/get?httpMethod=GET");

                from("direct:dynamicPathAndQuery")
                        .to("java-http://localhost:" + getWireMockPort() + "?httpMethod=GET");

                from("direct:methodOverride")
                        .to("java-http://localhost:" + getWireMockPort() + "/post?httpMethod=GET"); // Will be overridden
            }
        };
    }
}
