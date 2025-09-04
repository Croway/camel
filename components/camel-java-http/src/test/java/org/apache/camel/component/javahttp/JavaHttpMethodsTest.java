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

public class JavaHttpMethodsTest extends JavaHttpTestBase {

    @Test
    public void testGetMethod() throws Exception {
        wireMock.stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"method\": \"GET\", \"url\": \"" + baseUrl + "/get\"}")));

        Exchange exchange = template.request("direct:get", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
        assertNotNull(message.getBody(String.class));

        wireMock.verify(getRequestedFor(urlEqualTo("/get")));
    }

    @Test
    public void testPostMethod() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/post"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"method\": \"POST\", \"json\": {\"test\": \"data\"}}")));

        Exchange exchange = template.request("direct:post", exchange1 -> {
            exchange1.getIn().setBody("{\"test\":\"data\"}");
            exchange1.getIn().setHeader("Content-Type", "application/json");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
        assertNotNull(message.getBody(String.class));

        wireMock.verify(postRequestedFor(urlEqualTo("/post"))
                .withRequestBody(equalTo("{\"test\":\"data\"}")));
    }

    @Test
    public void testPutMethod() throws Exception {
        wireMock.stubFor(put(urlEqualTo("/put"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"method\": \"PUT\", \"json\": {\"test\": \"updated\"}}")));

        Exchange exchange = template.request("direct:put", exchange1 -> {
            exchange1.getIn().setBody("{\"test\":\"updated\"}");
            exchange1.getIn().setHeader("Content-Type", "application/json");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        wireMock.verify(putRequestedFor(urlEqualTo("/put"))
                .withRequestBody(equalTo("{\"test\":\"updated\"}")));
    }

    @Test
    public void testDeleteMethod() throws Exception {
        wireMock.stubFor(delete(urlEqualTo("/delete"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"method\": \"DELETE\"}")));

        Exchange exchange = template.request("direct:delete", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        wireMock.verify(deleteRequestedFor(urlEqualTo("/delete")));
    }

    @Test
    public void testMethodOverrideViaHeader() throws Exception {
        wireMock.stubFor(patch(urlEqualTo("/patch"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"method\": \"PATCH\", \"json\": {\"test\": \"patch\"}}")));

        Exchange exchange = template.request("direct:methodOverride", exchange1 -> {
            exchange1.getIn().setHeader(JavaHttpConstants.HTTP_METHOD, "PATCH");
            exchange1.getIn().setBody("{\"test\":\"patch\"}");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        wireMock.verify(patchRequestedFor(urlEqualTo("/patch"))
                .withRequestBody(equalTo("{\"test\":\"patch\"}")));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:get")
                        .toD("java-http://localhost:" + getWireMockPort() + "/get?httpMethod=GET");

                from("direct:post")
                        .toD("java-http://localhost:" + getWireMockPort() + "/post?httpMethod=POST");

                from("direct:put")
                        .toD("java-http://localhost:" + getWireMockPort() + "/put?httpMethod=PUT");

                from("direct:delete")
                        .toD("java-http://localhost:" + getWireMockPort() + "/delete?httpMethod=DELETE");

                from("direct:methodOverride")
                        .toD("java-http://localhost:" + getWireMockPort() + "/patch?httpMethod=GET"); // Will be overridden by header
            }
        };
    }
}
