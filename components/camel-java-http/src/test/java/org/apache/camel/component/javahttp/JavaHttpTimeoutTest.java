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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpTimeoutTest extends JavaHttpTestBase {

    @Test
    public void testRequestTimeout() {
        // Setup WireMock stub with delay to simulate timeout
        wireMock.stubFor(get(urlEqualTo("/delay/3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000) // 3 second delay
                        .withBody("Delayed response")));

        Exchange exchange = template.request("direct:timeout", ex -> {
            // This should timeout because we set requestTimeout=1000 and delay=3000
        });
        // Should get a timeout exception
        assertNotNull(exchange.getException());
        Assertions.assertThat(exchange.getException())
                .isInstanceOf(HttpOperationFailedException.class);
        Assertions.assertThat(((HttpOperationFailedException) exchange.getException()).getHttpResponseStatus())
                .isEqualTo("Request timeout");
    }

    @Test
    public void testConnectTimeout() {
        // Test connect timeout - using a non-routable IP address to force connection timeout
        Exchange exchange = template.request("direct:connectTimeout", ex -> {
            // This should timeout on connection to non-routable IP
        });
        assertNotNull(exchange.getException());
        Assertions.assertThat(exchange.getException())
                .isInstanceOf(HttpOperationFailedException.class);
        Assertions.assertThat(((HttpOperationFailedException) exchange.getException()).getHttpResponseStatus())
                .isEqualTo("Connection timeout");
    }

    @Test
    public void testSuccessfulRequestWithinTimeout() throws Exception {
        // Setup WireMock stub for successful request within timeout
        wireMock.stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\":true}")));

        Exchange exchange = template.request("direct:withinTimeout", exchange1 -> {
            // This should succeed within timeout
        });

        assertNotNull(exchange);
        assertNotNull(exchange.getMessage().getBody());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("direct:timeout")
                        .to("java-http://localhost:" + getWireMockPort() + "/delay/3?requestTimeout=1000&httpMethod=GET");

                from("direct:connectTimeout")
                        .to("java-http://10.255.255.1:80/test?connectTimeout=1000&httpMethod=GET");

                from("direct:withinTimeout")
                        .to("java-http://localhost:" + getWireMockPort()
                            + "/get?requestTimeout=10000&connectTimeout=5000&httpMethod=GET");
            }
        };
    }
}
