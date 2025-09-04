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

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpThrowExceptionOnFailureTest extends JavaHttpTestBase {

    @Test
    public void testHttpGetWhichReturns404WithoutException() throws Exception {
        // Setup WireMock stub for 404 status
        wireMock.stubFor(get(urlEqualTo("/status/404"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Not Found")));

        Exchange exchange = template.request("direct:noException", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertNotNull(message);

        Map<String, Object> headers = message.getHeaders();
        assertEquals(404, headers.get(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    @Test
    public void testHttpGetWhichReturns404ShouldThrowException() {
        // Setup WireMock stub for 404 status with exception throwing enabled
        wireMock.stubFor(get(urlEqualTo("/status/404"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Not Found")));

        Exchange exchange = template.request("direct:withException", ex -> {
            ex.getMessage().setHeader("test", "value");
        });

        assertInstanceOf(HttpOperationFailedException.class, exchange.getException());
        HttpOperationFailedException httpException = (HttpOperationFailedException) exchange.getException();
        assertEquals(404, httpException.getStatusCode());
    }

    @Test
    public void testHttpGetWhichReturns200() throws Exception {
        // Setup WireMock stub for successful response
        wireMock.stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"url\":\"http://localhost/get\",\"headers\":{}}")));

        Exchange exchange = template.request("direct:success", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertNotNull(message);

        Map<String, Object> headers = message.getHeaders();
        assertEquals(200, headers.get(JavaHttpConstants.HTTP_RESPONSE_CODE));
        assertNotNull(message.getBody(String.class));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:noException")
                        .to("java-http://localhost:" + getWireMockPort() + "/status/404?throwExceptionOnFailure=false");

                from("direct:withException")
                        .to("java-http://localhost:" + getWireMockPort() + "/status/404?throwExceptionOnFailure=true");

                from("direct:success")
                        .to("java-http://localhost:" + getWireMockPort() + "/get?httpMethod=GET");
            }
        };
    }
}
