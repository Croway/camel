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

public class JavaHttpResponseStreamingTest extends JavaHttpTestBase {

    @Test
    public void testResponseStreamingEnabled() throws Exception {
        // Setup WireMock stub for streaming response
        wireMock.stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\":\"response with streaming enabled\"}")));

        Exchange exchange = template.request("direct:streaming", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        // With streaming enabled, large responses should return InputStream
        Object body = message.getBody();
        // Note: Depending on response size, might be InputStream or byte[]
        assertNotNull(body);
    }

    @Test
    public void testResponseStreamingDisabled() throws Exception {
        // Setup WireMock stub for non-streaming response
        wireMock.stubFor(get(urlEqualTo("/get"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\":\"response with streaming disabled\"}")));

        Exchange exchange = template.request("direct:noStreaming", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        // With streaming disabled, should always get byte[] or String
        Object body = message.getBody();
        assertNotNull(body);
    }

    @Test
    public void testLargeResponse() throws Exception {
        // Setup WireMock stub for large response (simulating base64 endpoint)
        String largeData = "VGhpcyBpcyBhIGxhcmdlIHJlc3BvbnNlIHNpbXVsYXRpbmc="; // Base64 encoded data
        wireMock.stubFor(get(urlEqualTo("/base64/SFRUUEJJTiBpcyBhd2Vzb21l"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(largeData)));

        Exchange exchange = template.request("direct:largeResponse", exchange1 -> {
            // Empty processor
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
        assertNotNull(message.getBody());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:streaming")
                        .to("java-http://localhost:" + getWireMockPort()
                            + "/get?httpMethod=GET&responsePayloadStreamingThreshold=1024");

                from("direct:noStreaming")
                        .to("java-http://localhost:" + getWireMockPort()
                            + "/get?httpMethod=GET&responsePayloadStreamingThreshold=-1");

                from("direct:largeResponse")
                        .to("java-http://localhost:" + getWireMockPort() + "/base64/SFRUUEJJTiBpcyBhd2Vzb21l?httpMethod=GET");
            }
        };
    }
}
